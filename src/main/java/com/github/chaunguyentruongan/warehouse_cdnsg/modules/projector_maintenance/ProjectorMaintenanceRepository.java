package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectorMaintenanceRepository extends JpaRepository<ProjectorMaintenanceDetail, Long> {

    // SỬA LỖI: Join sang ticket (t) để lấy ngày startDate vì maintenanceDate không
    // còn ở bảng chi tiết
    @Query("SELECT m FROM ProjectorMaintenanceDetail m JOIN FETCH m.ticket t WHERE m.projector.id = :projectorId ORDER BY t.startDate DESC")
    List<ProjectorMaintenanceDetail> findHistoryByProjectorId(@Param("projectorId") Long projectorId);

    // SỬA LỖI UnknownPathException: Thêm JOIN m.ticket t và đổi m.technician thành
    // t.technician
    @Query("SELECT m FROM ProjectorMaintenanceDetail m JOIN FETCH m.projector p JOIN m.ticket t WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(p.serialNumber) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
            "LOWER(t.technician) LIKE LOWER(CONCAT('%', :kw, '%'))")
    Page<ProjectorMaintenanceDetail> searchHistory(@Param("kw") String kw, Pageable pageable);

    void deleteByProjectorId(Long id);

    void deleteByTicketId(Long id);
}