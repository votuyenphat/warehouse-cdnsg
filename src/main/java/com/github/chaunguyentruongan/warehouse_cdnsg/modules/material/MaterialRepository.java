package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Optional<Material> findByIdWithLock(@Param("id") Long id);

    // CẬP NHẬT: Thêm câu query lọc kết hợp
    @Query("SELECT m FROM Material m WHERE " +
            "(:kw IS NULL OR :kw = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :kw, '%'))) AND " +
            "(:status IS NULL OR :status = '' OR " +
            "(:status = 'LOW' AND m.inventory < 5) OR " +
            "(:status = 'OK' AND m.inventory >= 5))")
    Page<Material> searchWithFilter(@Param("kw") String kw, @Param("status") String status, Pageable pageable);

    @Query("SELECT new com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.TopMaterialDTO(m, SUM(ei.quantity)) "
            +
            "FROM ExportItem ei " +
            "JOIN ei.material m " +
            "JOIN ei.exportReceipt er " +
            "WHERE er.status = 'COMPLETED' AND m.deleted = false " + // Chỉ tính các phiếu đã hoàn thành và sản phẩm
                                                                     // chưa bị xóa
            "GROUP BY m " +
            "ORDER BY SUM(ei.quantity) DESC")
    List<TopMaterialDTO> findTopExportedMaterials(Pageable pageable);

    @Query("SELECT COALESCE(SUM(m.inventory), 0) FROM Material m")
    long sumTotalInventory();

    @Query("SELECT COUNT(m) FROM Material m WHERE m.inventory < 5")
    long countLowStock();

    @Query("SELECT COUNT(m) FROM Material m WHERE m.inventory >= 5")
    long countOkStock();
}