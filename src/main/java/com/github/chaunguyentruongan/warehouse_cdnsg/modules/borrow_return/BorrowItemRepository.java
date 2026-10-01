package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface BorrowItemRepository extends JpaRepository<BorrowItem, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM BorrowItem i WHERE i.id = :id")
    Optional<BorrowItem> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT i FROM BorrowItem i WHERE " +
           "(:kw IS NULL OR :kw = '' OR LOWER(i.name) LIKE LOWER(CONCAT('%', :kw, '%'))) AND " +
           "(:category IS NULL OR :category = '' OR i.category = :category) AND " +
           "(:availableOnly = false OR i.availableQuantity > 0)")
    Page<BorrowItem> searchWithFilter(@Param("kw") String kw, @Param("category") String category, @Param("availableOnly") boolean availableOnly, Pageable pageable);

    @Query("SELECT i FROM BorrowItem i WHERE i.availableQuantity <= :threshold")
    java.util.List<BorrowItem> findLowStockItems(@Param("threshold") int threshold);

    @Query("SELECT COALESCE(SUM(i.totalQuantity), 0) FROM BorrowItem i")
    long sumTotalQuantity();

    @Query("SELECT COALESCE(SUM(i.availableQuantity), 0) FROM BorrowItem i")
    long sumAvailableQuantity();
}
