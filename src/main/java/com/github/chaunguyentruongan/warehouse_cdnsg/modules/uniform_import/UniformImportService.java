package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_import;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_core.UniformService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UniformImportService {
    private UniformImportRepository importRepository;
    private UniformService uniformService;

    public UniformImportService(UniformImportRepository importRepository, @Lazy UniformService uniformService) {
        this.importRepository = importRepository;
        this.uniformService = uniformService;
    }

    public UniformImport findById(Long id) {
        return importRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found import id: " + id));
    }

    public Page<UniformImport> getAll(LocalDate from, LocalDate to, String note, String name, Pageable page) {
        return importRepository.searchAndFilter(from, to, note, name, page);
    }

    @Transactional
    public UniformImport create(UniformImportRequest request) {
        UniformImport uniformImport = new UniformImport();
        uniformImport.setDate(request.getDate());
        uniformImport.setNote(request.getNote());
        uniformImport.setNameResponse(request.getNameResponse());

        List<UniformImportDetail> details = request.getDetails().stream().map(itemReq -> {
            uniformService.updateStock(itemReq.getUniformId(), itemReq.getQuantity()); // Cộng (+) kho
            return UniformImportDetail.builder()
                    .uniform(uniformService.findById(itemReq.getUniformId()))
                    .uniformImport(uniformImport)
                    .quantity(itemReq.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        uniformImport.setDetails(details);
        return importRepository.save(uniformImport);
    }

    @Transactional
    public UniformImport update(Long id, UniformImportRequest request) {
        UniformImport existing = findById(id);

        for (UniformImportDetail oldItem : existing.getDetails()) {
            uniformService.updateStock(oldItem.getUniform().getId(), -oldItem.getQuantity()); // Hoàn tác: Trừ (-)
        }
        existing.getDetails().clear();

        existing.setDate(request.getDate());
        existing.setNote(request.getNote());
        existing.setNameResponse(request.getNameResponse());

        for (UniformImportRequest.Detail itemReq : request.getDetails()) {
            uniformService.updateStock(itemReq.getUniformId(), itemReq.getQuantity()); // Áp dụng: Cộng (+)
            UniformImportDetail newItem = UniformImportDetail.builder()
                    .uniformImport(existing)
                    .uniform(uniformService.findById(itemReq.getUniformId()))
                    .quantity(itemReq.getQuantity())
                    .build();
            existing.getDetails().add(newItem);
        }
        return importRepository.save(existing);
    }

    // ĐÃ SỬA: Chuyển thành Xóa cứng (Hard Delete) thay vì đổi Status
    @Transactional
    public void delete(Long id) {
        UniformImport existing = findById(id);
        // 1. Hoàn tác kho (Xóa phiếu nhập kho thì phải TRỪ đi số lượng đã nhập)
        for (UniformImportDetail item : existing.getDetails()) {
            uniformService.updateStock(item.getUniform().getId(), -item.getQuantity());
        }
        // 2. Xóa cứng hoàn toàn khỏi DB
        importRepository.delete(existing);
    }
}