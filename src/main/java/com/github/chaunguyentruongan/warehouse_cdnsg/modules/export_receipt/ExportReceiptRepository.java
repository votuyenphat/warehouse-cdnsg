package com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExportReceiptRepository extends JpaRepository<ExportReceipt, Long> {

        long countByExportDate(LocalDate exportDate);

        long countByExportDateBetween(LocalDate fromDate, LocalDate toDate);

        @Modifying
        @Query("DELETE FROM ExportItem i WHERE i.material.id = :materialId")
        void deleteItemsByMaterialId(@Param("materialId") Long materialId);

        @Query("SELECT e FROM ExportReceipt e WHERE " +
                        "e.status = ReceiptStatus.COMPLETED AND " +
                        "(:fromDate IS NULL OR e.exportDate >= :fromDate) AND " +
                        "(:toDate IS NULL OR e.exportDate <= :toDate) AND " +
                        "(:keyword IS NULL OR :keyword = '' OR " +
                        "(LOWER(e.note) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(e.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(e.receiptCode) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
        Page<ExportReceipt> searchAndFilter(
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        @Param("keyword") String keyword,
                        Pageable pageable);
}