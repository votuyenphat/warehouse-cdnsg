package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowTicketRepository extends JpaRepository<BorrowTicket, Long> {

        Optional<BorrowTicket> findByBorrowCode(String borrowCode);

        Optional<BorrowTicket> findByReturnCode(String returnCode);

        long countByStatus(TicketStatus status);

        long countByEmail(String email);

        long countByEmailAndStatus(String email, TicketStatus status);

        @Query("SELECT COUNT(t) FROM BorrowTicket t WHERE t.email = :email AND (t.status = 'BORROWED' OR t.status = 'OVERDUE')")
        long countHoldingItemsByEmail(@Param("email") String email);

        @Query("SELECT t FROM BorrowTicket t WHERE t.email = :email AND (:status IS NULL OR t.status = :status)")
        Page<BorrowTicket> findByEmailAndOptionalStatus(@Param("email") String email, @Param("status") TicketStatus status, Pageable pageable);

        @Query("SELECT t FROM BorrowTicket t WHERE t.status = 'BORROWED' AND t.borrowTime < :limitDate")
        List<BorrowTicket> findOverdueTickets(@Param("limitDate") LocalDateTime limitDate);

        @org.springframework.data.jpa.repository.Modifying
        @jakarta.transaction.Transactional
        @Query("UPDATE BorrowTicket t SET t.status = 'OVERDUE' WHERE t.status = 'BORROWED' AND t.expectedReturnDate < :currentDate")
        int updateOverdueTickets(@Param("currentDate") java.time.LocalDate currentDate);

        // Đã sửa lại logic tìm kiếm trong danh sách items bằng EXISTS
        @Query("SELECT t FROM BorrowTicket t WHERE " +
                        "(:status IS NULL OR t.status = :status) AND " +
                        "(:kw IS NULL OR :kw = '' OR " +
                        "LOWER(t.borrowerName) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
                        "LOWER(t.borrowCode) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
                        "LOWER(t.returnCode) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
                        "EXISTS (SELECT 1 FROM t.items item WHERE LOWER(item.itemName) LIKE LOWER(CONCAT('%', :kw, '%'))))")
        Page<BorrowTicket> searchWithFilter(
                        @Param("kw") String kw,
                        @Param("status") TicketStatus status,
                        Pageable pageable);
}