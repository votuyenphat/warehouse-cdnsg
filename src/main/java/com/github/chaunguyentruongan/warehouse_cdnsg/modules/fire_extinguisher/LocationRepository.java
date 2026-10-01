package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    // Lấy danh sách các vị trí theo ID Khu vực
    List<Location> findByZoneId(Long zoneId);
}