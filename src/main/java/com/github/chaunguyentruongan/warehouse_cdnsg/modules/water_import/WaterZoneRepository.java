package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaterZoneRepository extends JpaRepository<WaterZone, Long> {
}
