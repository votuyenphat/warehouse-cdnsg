package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_import;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface UniformImportRepository extends JpaRepository<UniformImport, Long> {
        @Query("SELECT u FROM UniformImport u WHERE " +
                        "(:fromDate IS NULL OR u.date >= :fromDate) AND " +
                        "(:toDate IS NULL OR u.date <= :toDate) AND " +
                        "(:note IS NULL OR LOWER(u.note) LIKE LOWER(CONCAT('%', :note, '%'))) AND " +
                        "(:name IS NULL OR LOWER(u.nameResponse) LIKE LOWER(CONCAT('%', :name, '%')))")
        Page<UniformImport> searchAndFilter(
                        @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate,
                        @Param("note") String note, @Param("name") String name, Pageable pageable);
}