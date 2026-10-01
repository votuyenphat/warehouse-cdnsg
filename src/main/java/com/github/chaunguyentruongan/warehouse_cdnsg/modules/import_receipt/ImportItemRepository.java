package com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.RecentTransactionDTO;

public interface ImportItemRepository extends JpaRepository<ImportItem, Long> {

        @Query("SELECT new com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.RecentTransactionDTO(" +
                        "m.id, m.name, u.name, ii.quantity, r.importDate, r.id, r.receiptCode, 'IMPORT') " +
                        "FROM ImportItem ii " +
                        "JOIN ii.material m " +
                        "LEFT JOIN m.unit u " +
                        "JOIN ii.importReceipt r " +
                        "WHERE r.status = com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus.COMPLETED "
                        +
                        "ORDER BY r.importDate DESC, r.id DESC")
        List<RecentTransactionDTO> findRecentImports(Pageable pageable);

        @Query("SELECT new com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.RecentTransactionDTO(" +
                        "m.id, m.name, u.name, ii.quantity, r.importDate, r.id, r.receiptCode, 'IMPORT') " +
                        "FROM ImportItem ii " +
                        "JOIN ii.material m " +
                        "LEFT JOIN m.unit u " +
                        "JOIN ii.importReceipt r " +
                        "WHERE m.id = :materialId AND r.status = com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus.COMPLETED "
                        +
                        "ORDER BY r.importDate DESC, r.id DESC")
        List<RecentTransactionDTO> findImportHistoryByMaterialId(@Param("materialId") Long materialId);

        @Query("SELECT new com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt.ImportReportFlatDTO(" +
                        "r.importDate, r.receiptCode, m.name, u.name, ii.quantity, r.note) " +
                        "FROM ImportItem ii " +
                        "JOIN ii.importReceipt r " +
                        "JOIN ii.material m " +
                        "LEFT JOIN m.unit u " +
                        "WHERE r.status = com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus.COMPLETED "
                        +
                        "AND (:fromDate IS NULL OR r.importDate >= :fromDate) " +
                        "AND (:toDate IS NULL OR r.importDate <= :toDate) " +
                        "AND (:materialName IS NULL OR :materialName = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :materialName, '%'))) "
                        +
                        "AND (:note IS NULL OR :note = '' OR LOWER(r.note) LIKE LOWER(CONCAT('%', :note, '%'))) " +
                        "AND (:quantity IS NULL OR ii.quantity = :quantity) " +
                        "ORDER BY r.importDate DESC, r.id DESC")
        List<ImportReportFlatDTO> getImportReportData(
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        @Param("materialName") String materialName,
                        @Param("note") String note,
                        @Param("quantity") Integer quantity);
}