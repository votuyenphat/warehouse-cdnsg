package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
}