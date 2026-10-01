package com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryFileRepository extends JpaRepository<InventoryFile, Long> {

    /**
     * Tìm kiếm file theo phòng ban và keyword (tìm trong fileName).
     * Chỉ trả về file chưa bị xóa mềm.
     */
    @Query("SELECT f FROM InventoryFile f WHERE f.deleted = false " +
           "AND (:department IS NULL OR LOWER(f.departmentName) = LOWER(:department)) " +
           "AND (:keyword IS NULL OR LOWER(f.fileName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY f.updatedAt DESC, f.createdAt DESC")
    Page<InventoryFile> searchFiles(
            @Param("department") String department,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Lấy danh sách tên phòng ban phân biệt - dùng để hiển thị filter dropdown trên Frontend.
     */
    @Query("SELECT DISTINCT f.departmentName FROM InventoryFile f WHERE f.deleted = false ORDER BY f.departmentName")
    List<String> findAllDistinctDepartments();
}
