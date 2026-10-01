package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/water-zones")
@RequiredArgsConstructor
public class WaterZoneController {
    private final WaterZoneRepository waterZoneRepository;

    @GetMapping
    public ResponseEntity<List<WaterZone>> getAllZones() {
        return ResponseEntity.ok(waterZoneRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<WaterZone> createZone(@RequestBody WaterZone zone) {
        return ResponseEntity.ok(waterZoneRepository.save(zone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WaterZone> updateZone(@PathVariable Long id, @RequestBody WaterZone zoneDetails) {
        WaterZone zone = waterZoneRepository.findById(id).orElseThrow();
        zone.setName(zoneDetails.getName());
        zone.setDescription(zoneDetails.getDescription());
        return ResponseEntity.ok(waterZoneRepository.save(zone));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteZone(@PathVariable Long id) {
        waterZoneRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
