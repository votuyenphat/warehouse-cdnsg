package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaterImportRepository extends JpaRepository<WaterImport, Long> {

    @Query("SELECT w FROM WaterImport w WHERE " +
           "(:fromDate IS NULL OR w.importDate >= :fromDate) AND " +
           "(:toDate IS NULL OR w.importDate <= :toDate)")
    Page<WaterImport> searchWithFilters(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    @Query("SELECT w FROM WaterImport w WHERE " +
           "(:fromDate IS NULL OR w.importDate >= :fromDate) AND " +
           "(:toDate IS NULL OR w.importDate <= :toDate) " +
           "ORDER BY w.importDate ASC")
    List<WaterImport> findAllBetweenDates(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
