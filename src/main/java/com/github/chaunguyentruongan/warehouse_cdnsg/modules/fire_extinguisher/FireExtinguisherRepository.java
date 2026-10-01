package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FireExtinguisherRepository extends JpaRepository<FireExtinguisher, Long> {

        @Query("SELECT f FROM FireExtinguisher f " +
                        "JOIN f.location l " +
                        "JOIN l.zone z " +
                        "WHERE (:kw IS NULL OR " +
                        "LOWER(f.type) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
                        "LOWER(l.name) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
                        "LOWER(z.name) LIKE LOWER(CONCAT('%', :kw, '%'))) " +
                        "AND (:zoneId IS NULL OR z.id = :zoneId) " +
                        "AND (:type IS NULL OR LOWER(f.type) LIKE LOWER(CONCAT('%', :type, '%'))) " +
                        "AND (:weight IS NULL OR f.weight = :weight) " + // Đã thêm khoảng trắng ở cuối
                        "AND (:status IS NULL OR f.status = :status)" // Đã thêm điều kiện IS NULL cho status
        )
        Page<FireExtinguisher> searchWithFilters(
                        @Param("kw") String kw,
                        @Param("zoneId") Long zoneId,
                        @Param("type") String type,
                        @Param("weight") String weight,
                        @Param("status") MaintenanceStatus status,
                        Pageable pageable);

        long countByStatus(MaintenanceStatus status);

        Page<FireExtinguisher> findByLocationId(Long locationId, Pageable pageable);

        Page<FireExtinguisher> findByLocationZoneId(Long zoneId, Pageable pageable);

        List<FireExtinguisher> findAllByLocationZoneId(Long zoneId);

        @Query("SELECT new com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher.ZoneExtinguisherStatsResponse("
                        +
                        "z.name, f.type, SUM(f.quantity), MAX(f.lastRechargeDate), MIN(f.nextRechargeDate)) " +
                        "FROM FireExtinguisher f JOIN f.location l JOIN l.zone z " +
                        "GROUP BY z.name, f.type")
        List<ZoneExtinguisherStatsResponse> getAdvancedStats();
}