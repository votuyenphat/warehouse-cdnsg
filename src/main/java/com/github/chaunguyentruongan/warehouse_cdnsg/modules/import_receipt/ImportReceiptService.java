package com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt;

import com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus;
import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.MaterialService;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportReceiptService {
    private final ImportReceiptRepository importReceiptRepository;
    private final MaterialService materialService;
    private final ImportItemRepository importItemRepository;

    public List<ImportReceipt> findByImportDateBetween(LocalDate from, LocalDate to) {
        return importReceiptRepository.findByImportDateBetween(from, to);
    }

    public ImportReceipt findById(Long id) {
        return importReceiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found import receipt by id: " + id));
    }

    // CREATE
    @Transactional
    // Trích xuất trong hàm create() của ImportReceiptService.java
    public ImportReceipt create(ImportReceiptRequest request) {
        ImportReceipt importReceipt = new ImportReceipt();
        importReceipt.setImportDate(request.getImportDate());
        importReceipt.setNote(request.getNote());

        long countToday = importReceiptRepository.countByImportDate(request.getImportDate());
        String dateStr = request.getImportDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        if (request.getInvoiceCode() == null || request.getInvoiceCode().trim().isEmpty()) {
            String receiptCode = String.format("PN-%s-%03d", dateStr, countToday + 1);
            importReceipt.setReceiptCode(receiptCode);
        } else {
            importReceipt.setReceiptCode(request.getInvoiceCode().trim());
        }

        // 2. NGƯỜI LẬP PHIẾU (Tạm gán cứng, sau này lấy từ Spring Security Token)
        importReceipt.setCreatedBy("Admin");
        importReceipt.setStatus(ReceiptStatus.COMPLETED);

        List<ImportItem> importItems = request.getImportItemRequests()
                .stream()
                .map(itemReq -> {
                    // 1. Cập nhật tồn kho TRONG material (cộng thêm)
                    materialService.updateStock(itemReq.getMaterialId(), itemReq.getQuantity());

                    // 2. Build ImportItem
                    return ImportItem.builder()
                            .material(materialService.findById(itemReq.getMaterialId()))
                            .importReceipt(importReceipt)
                            .quantity(itemReq.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        importReceipt.setImportItems(importItems);
        return importReceiptRepository.save(importReceipt);
    }

    // READ - Lọc & Phân trang
    public Page<ImportReceipt> getAll(LocalDate fromDate, LocalDate toDate, String keyword, Pageable pageable) {
        return importReceiptRepository.searchAndFilter(fromDate, toDate, keyword, pageable);
    }

    // UPDATE
    @Transactional
    public ImportReceipt update(Long id, ImportReceiptRequest request) {
        ImportReceipt existing = findById(id);

        // BƯỚC 1: HOÀN TÁC TRẠNG THÁI CŨ
        // Trừ đi số lượng tồn kho của các item CŨ đã từng được cộng vào trước đó
        for (ImportItem oldItem : existing.getImportItems()) {
            materialService.updateStock(oldItem.getMaterial().getId(), -oldItem.getQuantity());
        }

        // Xóa sạch các item cũ trong danh sách hiện tại để JPA xóa chúng trong database
        // (nhờ orphanRemoval = true)
        existing.getImportItems().clear();

        // BƯỚC 2: CẬP NHẬT THÔNG TIN PHIẾU
        existing.setImportDate(request.getImportDate());
        existing.setNote(request.getNote());

        // BƯỚC 3: ÁP DỤNG TRẠNG THÁI MỚI
        // Lặp qua danh sách request mới, cộng kho và tạo item mới
        for (ImportItemRequest itemReq : request.getImportItemRequests()) {

            // Cộng tồn kho theo số lượng MỚI
            materialService.updateStock(itemReq.getMaterialId(), itemReq.getQuantity());

            // Tạo Entity ImportItem mới và thêm vào danh sách
            ImportItem newItem = ImportItem.builder()
                    .importReceipt(existing)
                    .material(materialService.findById(itemReq.getMaterialId()))
                    .quantity(itemReq.getQuantity())
                    .build();

            existing.getImportItems().add(newItem);
        }

        // Lưu lại phiếu nhập cùng các item mới
        return importReceiptRepository.save(existing);
    }

    // DELETE
    @Transactional
    public void delete(Long id) {
        ImportReceipt existing = findById(id);

        if (existing.getStatus() == ReceiptStatus.CANCELLED) {
            throw new RuntimeException("Phiếu này đã bị hủy từ trước!");
        }

        existing.setStatus(ReceiptStatus.CANCELLED);

        // 1. Hoàn tác (trừ đi) tồn kho của tất cả các mặt hàng trong phiếu nhập này
        for (ImportItem item : existing.getImportItems()) {
            // Truyền vào số âm để trừ đi
            materialService.updateStock(item.getMaterial().getId(), -item.getQuantity());
        }

        // 2. Sau khi đã trừ kho thành công, tiến hành xóa phiếu nhập
        importReceiptRepository.save(existing);
    }

    @Transactional
    public void deleteByMaterialId(Long id) {
        // Xóa trực tiếp các dòng chi tiết trong phiếu nhập liên quan đến vật tư này
        importReceiptRepository.deleteItemsByMaterialId(id);
    }


    public List<DailyImportReportDTO> getDailyImportReport(
            LocalDate fromDate, LocalDate toDate, String materialName, String note, Integer quantity) {

        // 1. Gọi query lấy danh sách phẳng
        List<ImportReportFlatDTO> flatData = importItemRepository.getImportReportData(
                fromDate, toDate, materialName, note, quantity);

        // 2. Nhóm dữ liệu theo ngày (Group By importDate)
        Map<LocalDate, List<ImportReportFlatDTO>> groupedByDate = flatData.stream()
                .collect(Collectors.groupingBy(ImportReportFlatDTO::getImportDate));

        // 3. Map sang DTO chuẩn và sắp xếp ngày mới nhất lên đầu
        return groupedByDate.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, List<ImportReportFlatDTO>>comparingByKey().reversed())
                .map(entry -> {
                    LocalDate date = entry.getKey();

                    // Chuyển FlatDTO thành DetailDTO
                    List<ImportReportDetailDTO> details = entry.getValue().stream()
                            .map(flat -> new ImportReportDetailDTO(
                                    flat.getReceiptCode(),
                                    flat.getMaterialName(),
                                    flat.getUnitName(),
                                    flat.getQuantity(),
                                    flat.getNote()))
                            .collect(Collectors.toList());

                    // Tính tổng số lượng của ngày hôm đó
                    int totalQuantity = details.stream().mapToInt(ImportReportDetailDTO::getQuantity).sum();

                    return new DailyImportReportDTO(date, totalQuantity, details);
                })
                .collect(Collectors.toList());
    }
}
