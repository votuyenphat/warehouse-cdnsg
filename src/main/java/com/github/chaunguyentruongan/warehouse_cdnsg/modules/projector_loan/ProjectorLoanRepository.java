package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectorLoanRepository extends JpaRepository<ProjectorLoan, Long> {
        // Truy xuất toàn bộ lịch sử mượn của 1 máy chiếu cụ thể
        List<ProjectorLoan> findByProjectorIdOrderByBorrowDateDesc(Long projectorId);

        // Tìm các phiếu đang mượn (chưa trả)
        Page<ProjectorLoan> findByStatus(LoanStatus status, Pageable pageable);

        // Thêm câu query hỗ trợ lọc theo trạng thái phiếu mượn và tên người mượn
        @Query("SELECT l FROM ProjectorLoan l WHERE " +
                        "(:status IS NULL OR l.status = :status) AND " +
                        "(:kw IS NULL OR LOWER(l.borrower) LIKE LOWER(CONCAT('%', :kw, '%')))")
        Page<ProjectorLoan> searchWithFilter(@Param("kw") String kw, @Param("status") LoanStatus status,
                        Pageable pageable);

        // Thêm vào ProjectorLoanRepository.java
        @Query(value = "SELECT p.id as projectorId, p.name as projectorName, p.serial_number as serialNumber, " +
                        "SUM(TIMESTAMPDIFF(SECOND, l.borrow_date, l.return_date)) as totalUsageSeconds " +
                        "FROM projector p " +
                        "JOIN projector_loan l ON p.id = l.projector_id " +
                        "WHERE l.status = 'RETURNED' " +
                        "GROUP BY p.id, p.name, p.serial_number", nativeQuery = true)
        List<Object[]> getUsageStatsNative();

}