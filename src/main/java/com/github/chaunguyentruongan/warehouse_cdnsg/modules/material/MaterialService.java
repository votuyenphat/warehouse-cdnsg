package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.exception.SqlDuplicateException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt.ImportItemRepository;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt.ImportReceiptService;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt.ExportItemRepository;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt.ExportReceiptService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class MaterialService {
    private final MaterialRepository materialRepository;
    private final ImportReceiptService importReceiptService;
    private final ExportReceiptService exportReceiptService;
    private final UnitService unitService;

    private final ImportItemRepository importItemRepository;
    private final ExportItemRepository exportItemRepository;

    public MaterialService(MaterialRepository materialRepository, @Lazy ImportReceiptService importReceiptService,
            @Lazy ExportReceiptService exportReceiptService, UnitService unitService,
            ImportItemRepository importItemRepository, ExportItemRepository exportItemRepository) {
        this.materialRepository = materialRepository;
        this.importReceiptService = importReceiptService;
        this.exportReceiptService = exportReceiptService;
        this.unitService = unitService;
        this.importItemRepository = importItemRepository;
        this.exportItemRepository = exportItemRepository;
    }

    public Material create(MaterialRequestCreate request) {
        try {
            Material newMaterial = new Material();
            newMaterial.setName(request.getName());
            newMaterial.setUnit(unitService.findById(request.getUnit_id()));
            newMaterial.setInventory(0);
            newMaterial.setDeleted(false);
            return materialRepository.save(newMaterial);
        } catch (Exception e) {
            throw new SqlDuplicateException(e.getMessage());
        }
    }

    public Material findById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found material id: " + id));
    }

    public Page<Material> findAll(Pageable pageable) {
        return materialRepository.findAll(pageable);
    }

    public Material update(Long id, MaterialRequestCreate update) {
        Material material = findById(id);

        if (update.getUnit_id() != material.getUnit().getId()) {
            Unit unit = unitService.findById(update.getUnit_id());
            material.setUnit(unit);
        }
        material.setName(update.getName());
        return materialRepository.save(material);
    }

    @Transactional
    public void delete(Long id) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy vật tư ID: " + id));

        materialRepository.delete(material);
    }

    @Transactional
    public void updateStock(Long materialId, int quantityChange) {
        // Sử dụng hàm có Lock để chống đụng độ dữ liệu khi nhiều người thao tác cùng
        // lúc
        Material material = materialRepository.findByIdWithLock(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found material id: " + materialId));

        int newQuantity = material.getInventory() + quantityChange;

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Tồn kho không đủ cho vật tư: " + material.getName());
        }

        material.setInventory(newQuantity);
        materialRepository.save(material);
    }

    // Thay thế đoạn findAll cũ bằng đoạn này
    public Page<Material> findAll(String keyword, String status, Pageable pageable) {
        return materialRepository.searchWithFilter(keyword, status, pageable);
    }

    public java.util.Map<String, Object> getMaterialStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        List<Object[]> results = materialRepository.getAggregatedMaterialStats();
        if (results != null && !results.isEmpty() && results.get(0) != null) {
            Object[] row = results.get(0);
            stats.put("totalTypes", row[0] != null ? ((Number) row[0]).longValue() : 0L);
            stats.put("totalItems", row[1] != null ? ((Number) row[1]).longValue() : 0L);
            stats.put("lowStockCount", row[2] != null ? ((Number) row[2]).longValue() : 0L);
            stats.put("okStockCount", row[3] != null ? ((Number) row[3]).longValue() : 0L);
        } else {
            stats.put("totalTypes", 0L);
            stats.put("totalItems", 0L);
            stats.put("lowStockCount", 0L);
            stats.put("okStockCount", 0L);
        }
        return stats;
    }

    public List<TopMaterialDTO> getTop10MostUsedMaterials() {
        return materialRepository.findTopExportedMaterials(PageRequest.of(0, 10));
    }

    public RecentActivitiesResponse getRecentActivities() {
        // Lấy đúng 10 bản ghi đầu tiên
        PageRequest topTen = PageRequest.of(0, 10);

        List<RecentTransactionDTO> imports = importItemRepository.findRecentImports(topTen);
        List<RecentTransactionDTO> exports = exportItemRepository.findRecentExports(topTen);

        return new RecentActivitiesResponse(imports, exports);
    }

    public MaterialDetailResponseDTO getMaterialDetailsWithHistory(Long materialId) {
        // 1. Lấy thông tin cơ bản của vật tư
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật tư hoặc vật tư đã bị xóa!"));

        // 2. Lấy lịch sử nhập và xuất
        List<RecentTransactionDTO> imports = importItemRepository.findImportHistoryByMaterialId(materialId);
        List<RecentTransactionDTO> exports = exportItemRepository.findExportHistoryByMaterialId(materialId);

        // 3. Map sang DTO
        String unitName = material.getUnit() != null ? material.getUnit().getName() : null;

        return new MaterialDetailResponseDTO(
                material.getId(),
                material.getName(),
                unitName,
                material.getInventory(),
                imports,
                exports);
    }
}
