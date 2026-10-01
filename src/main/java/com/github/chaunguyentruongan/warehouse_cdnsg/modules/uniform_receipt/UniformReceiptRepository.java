package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_receipt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface UniformReceiptRepository extends JpaRepository<UniformReceipt, Long> {
        @Query("SELECT r FROM UniformReceipt r WHERE " +
                        "(:fromDate IS NULL OR r.date >= :fromDate) AND " +
                        "(:toDate IS NULL OR r.date <= :toDate) AND " +
                        "(:cusName IS NULL OR LOWER(r.cusName) LIKE LOWER(CONCAT('%', :cusName, '%')))")
        Page<UniformReceipt> searchAndFilter(
                        @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
                        @Param("cusName") String cusName, Pageable pageable);
}