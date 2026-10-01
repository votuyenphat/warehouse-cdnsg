package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projector-maintenances")
@RequiredArgsConstructor
@Tag(name = "Quản lý Bảo trì Máy chiếu")
public class ProjectorMaintenanceController {

    private final ProjectorMaintenanceService maintenanceService;

    @PostMapping
    public ResponseEntity<MaintenanceTicket> createTicket(@RequestBody MaintenanceTicketRequest request) {
        return ResponseEntity.ok(maintenanceService.createTicket(request));
    }

    @GetMapping
    public ResponseEntity<Page<MaintenanceTicket>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        return ResponseEntity.ok(maintenanceService.getAllTickets(keyword, status, pageable));
    }

    @GetMapping("/projector/{projectorId}")
    public ResponseEntity<List<ProjectorMaintenanceDetail>> getHistoryByProjector(@PathVariable Long projectorId) {
        return ResponseEntity.ok(maintenanceService.getHistoryByProjectorId(projectorId));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<MaintenanceTicket> completeTicket(
            @PathVariable Long id,
            @RequestBody CompleteMaintenanceRequest request) {
        return ResponseEntity.ok(maintenanceService.completeTicket(id, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintenanceTicket> updateTicket(
            @PathVariable Long id,
            @RequestBody MaintenanceTicketRequest request) {
        return ResponseEntity.ok(maintenanceService.updateTicket(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(
            @PathVariable Long id) {
        maintenanceService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}