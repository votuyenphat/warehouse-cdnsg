package com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.RecentTransactionDTO;

public interface ExportItemRepository extends JpaRepository<ExportItem, Long> {

        @Query("SELECT new com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.RecentTransactionDTO(" +
                        "m.id, m.name, u.name, ei.quantity, r.exportDate, r.id, r.receiptCode, 'EXPORT') " +
                        "FROM ExportItem ei " +
                        "JOIN ei.material m " +
                        "LEFT JOIN m.unit u " +
                        "JOIN ei.exportReceipt r " +
                        "WHERE r.status = com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus.COMPLETED "
                        +
                        "ORDER BY r.exportDate DESC, r.id DESC")
        List<RecentTransactionDTO> findRecentExports(Pageable pageable);

        @Query("SELECT new com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.RecentTransactionDTO(" +
                        "m.id, m.name, u.name, ei.quantity, r.exportDate, r.id, r.receiptCode, 'EXPORT') " +
                        "FROM ExportItem ei " +
                        "JOIN ei.material m " +
                        "LEFT JOIN m.unit u " +
                        "JOIN ei.exportReceipt r " +
                        "WHERE m.id = :materialId AND r.status = com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus.COMPLETED "
                        +
                        "ORDER BY r.exportDate DESC, r.id DESC")
        List<RecentTransactionDTO> findExportHistoryByMaterialId(@Param("materialId") Long materialId);

        @Query("SELECT new com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt.ExportReportFlatDTO(" +
                        "r.exportDate, r.receiptCode, m.name, u.name, ei.quantity, r.department, r.recipient, r.note) "
                        +
                        "FROM ExportItem ei " +
                        "JOIN ei.exportReceipt r " +
                        "JOIN ei.material m " +
                        "LEFT JOIN m.unit u " +
                        "WHERE r.status = com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus.COMPLETED "
                        +
                        "AND (:fromDate IS NULL OR r.exportDate >= :fromDate) " +
                        "AND (:toDate IS NULL OR r.exportDate <= :toDate) " +
                        "AND (:materialName IS NULL OR :materialName = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :materialName, '%'))) "
                        +
                        "AND (:note IS NULL OR :note = '' OR LOWER(r.note) LIKE LOWER(CONCAT('%', :note, '%'))) " +
                        "AND (:department IS NULL OR :department = '' OR LOWER(r.department) LIKE LOWER(CONCAT('%', :department, '%'))) "
                        +
                        "AND (:quantity IS NULL OR ei.quantity = :quantity) " +
                        "ORDER BY r.exportDate DESC, r.id DESC")
        List<ExportReportFlatDTO> getExportReportData(
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        @Param("materialName") String materialName,
                        @Param("note") String note,
                        @Param("department") String department,
                        @Param("quantity") Integer quantity);
}