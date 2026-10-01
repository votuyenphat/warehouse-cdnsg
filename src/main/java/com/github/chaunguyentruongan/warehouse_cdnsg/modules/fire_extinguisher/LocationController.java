package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationRepository locationRepository;

    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(locationRepository.findAll());
    }

    @GetMapping("/zone/{zoneId}")
    public ResponseEntity<List<Location>> getLocationsByZone(@PathVariable Long zoneId) {
        // API này rất hữu ích cho Frontend: Chọn Khu vực xong -> tự động load danh sách
        // Vị trí thuộc khu vực đó
        return ResponseEntity.ok(locationRepository.findByZoneId(zoneId));
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        return ResponseEntity.ok(locationRepository.save(location));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> updateLocation(@PathVariable Long id, @RequestBody Location locationDetails) {
        Location location = locationRepository.findById(id).orElseThrow();
        location.setName(locationDetails.getName());
        return ResponseEntity.ok(locationRepository.save(location));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}