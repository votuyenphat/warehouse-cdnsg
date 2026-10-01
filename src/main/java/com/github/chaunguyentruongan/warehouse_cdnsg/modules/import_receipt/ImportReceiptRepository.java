package com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ImportReceiptRepository extends JpaRepository<ImportReceipt, Long> {

       List<ImportReceipt> findByImportDateBetween(LocalDate from, LocalDate to);

       long countByImportDate(LocalDate importDate);

       @Modifying
       @Query("DELETE FROM ImportItem i WHERE i.material.id = :materialId")
       void deleteItemsByMaterialId(@Param("materialId") Long materialId);

       // Truy vấn kết hợp Lọc theo ngày, Tìm kiếm theo Note và Phân trang
       @Query("SELECT i FROM ImportReceipt i WHERE " +
                     "i.status <> com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus.CANCELLED AND " +
                     "(:fromDate IS NULL OR i.importDate >= :fromDate) AND " +
                     "(:toDate IS NULL OR i.importDate <= :toDate) AND " +
                     "(:keyword IS NULL OR :keyword = '' OR " +
                     "LOWER(i.note) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(i.receiptCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(i.receiptCode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
       Page<ImportReceipt> searchAndFilter(
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate,
                     @Param("keyword") String keyword,
                     Pageable pageable);
}
