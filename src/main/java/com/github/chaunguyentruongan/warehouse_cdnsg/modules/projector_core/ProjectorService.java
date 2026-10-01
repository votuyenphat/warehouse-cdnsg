package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.exception.SqlDuplicateException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_loan.ProjectorLoanService;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance.ProjectorMaintenanceService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectorService {
    private final ProjectorRepository projectorRepository;
    private final ProjectorLoanService projectorLoanService;
    private final ProjectorMaintenanceService projectorMaintenanceService;

    public Projector findById(Long id) {
        return projectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy máy chiếu với ID: " + id));
    }

    public List<Projector> findAll() {
        return projectorRepository.findAll();
    }

    // 1. LẤY DANH SÁCH (CÓ PHÂN TRANG VÀ TÌM KIẾM)
    public Page<Projector> getAll(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return projectorRepository.findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(keyword,
                    keyword, pageable);
        }
        return projectorRepository.findAll(pageable);
    }

    public Page<Projector> getAllWithFilter(String keyword, ProjectorStatus status, Pageable pageable) {
        return projectorRepository.searchWithFilter(keyword, status, pageable);
    }

    @Transactional
    public Projector create(ProjectorRequest request) {
        if (projectorRepository.existsBySerialNumber(request.getSerialNumber())) {
            throw new SqlDuplicateException("Số Serial này đã tồn tại trong hệ thống!");
        }

        Projector projector = Projector.builder()
                .name(request.getName())
                .serialNumber(request.getSerialNumber())
                .status(ProjectorStatus.AVAILABLE) // Mặc định khi mới mua về là sẵn sàng
                .build();
        return projectorRepository.save(projector);
    }

    // 2. CẬP NHẬT (SỬA THÔNG TIN)
    public Projector update(Long id, ProjectorRequest request) {
        Projector projector = findById(id);
        projector.setName(request.getName());
        projector.setSerialNumber(request.getSerialNumber());
        // Thường khi update thông tin cơ bản, không update status ở đây mà dùng API
        // updateStatus riêng
        return projectorRepository.save(projector);
    }

    // 3. XÓA MÁY CHIẾU
    @Transactional
    public void delete(Long id) {
        // Lưu ý: Nếu máy chiếu đã có lịch sử mượn/bảo trì, việc xóa cứng có thể gây lỗi
        // Foreign Key.
        // Bạn có thể cân nhắc thêm 1 trạng thái DELETED vào ProjectorStatus hoặc bắt
        // exception ở đây.
        projectorLoanService.deleteByProjectorId(id);
        projectorMaintenanceService.deleteByProjectorId(id);
        projectorRepository.deleteById(id);
    }

    // Xóa hàm getProjectorRepository cũ và thay bằng hàm này:
    public long countByStatus(ProjectorStatus status) {
        return projectorRepository.countByStatus(status);
    }

    @Transactional
    public Projector updateStatus(Long id, ProjectorStatus status) {
        Projector projector = findById(id);
        projector.setStatus(status);
        return projectorRepository.save(projector);
    }
}