package com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    /**
     * Lấy toàn bộ lịch sử thay đổi của một file, sắp xếp mới nhất trước.
     */
    Page<InventoryLog> findByFileIdOrderByCreatedAtDesc(Long fileId, Pageable pageable);

    /**
     * Lấy lịch sử thay đổi theo file và lọc theo người sửa.
     */
    @Query("SELECT l FROM InventoryLog l WHERE l.file.id = :fileId " +
           "AND (:changedBy IS NULL OR LOWER(l.changedBy) = LOWER(:changedBy)) " +
           "ORDER BY l.createdAt DESC")
    Page<InventoryLog> findByFileIdAndChangedBy(
            @Param("fileId") Long fileId,
            @Param("changedBy") String changedBy,
            Pageable pageable
    );
}
