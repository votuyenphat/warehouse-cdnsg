package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_core;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.exception.SqlDuplicateException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_import.UniformImportService;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_receipt.UniformReceiptService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniformService {
    private final UniformRepository uniformRepository;

    public Uniform create(UniformRequestCreate request) {
        try {
            Uniform uniform = Uniform.builder()
                    .type(request.getType())
                    .size(request.getSize())
                    .stock(0L) // Tồn kho mặc định bằng 0
                    .build();
            return uniformRepository.save(uniform);
        } catch (Exception e) {
            throw new SqlDuplicateException("Đồng phục loại này với size này đã tồn tại!");
        }
    }

    public Uniform findById(Long id) {
        return uniformRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found uniform id: " + id));
    }

    public Page<Uniform> findAll(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return uniformRepository.findByTypeContainingIgnoreCaseOrSizeContainingIgnoreCase(
                    keyword.trim(), keyword.trim(), pageable);
        }
        return uniformRepository.findAll(pageable);
    }

    // Giữ lại hàm này nếu các service khác cần lấy toàn bộ list không phân trang
    public List<Uniform> findAll() {
        return uniformRepository.findAll();
    }

    public Uniform update(Long id, UniformRequestCreate request) {
        Uniform uniform = findById(id);
        uniform.setType(request.getType());
        uniform.setSize(request.getSize());
        return uniformRepository.save(uniform);
    }

    public void delete(Long id) {

        uniformRepository.deleteById(id);
    }

    @Transactional
    public void updateStock(Long id, Long quantityChange) {
        Uniform uniform = uniformRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found uniform id: " + id));

        long newStock = uniform.getStock() + quantityChange;
        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Tồn kho không đủ cho đồng phục: " + uniform.getType() + " (Size: " + uniform.getSize() + ")");
        }

        uniform.setStock(newStock);
        uniformRepository.save(uniform);
    }
}