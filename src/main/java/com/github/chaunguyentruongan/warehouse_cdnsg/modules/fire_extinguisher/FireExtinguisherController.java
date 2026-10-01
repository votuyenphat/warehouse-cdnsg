package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fire-extinguishers")
@RequiredArgsConstructor
@Tag(name = "Quản lý Phòng cháy chữa cháy")
public class FireExtinguisherController {

    private final FireExtinguisherService service;

    @Operation(summary = "Lấy chi tiết 1 bình chữa cháy")
    @GetMapping("/{id}")
    public ResponseEntity<FireExtinguisherResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Xem lịch sử nạp của 1 bình")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ExtinguisherHistory>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(service.getHistory(id));
    }

    @Operation(summary = "Cập nhật thông tin cơ bản của bình")
    @PutMapping("/{id}")
    public ResponseEntity<FireExtinguisherResponse> update(@PathVariable Long id,
            @RequestBody FireExtinguisherRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Xóa bình chữa cháy")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lấy danh sách bình chữa cháy có phân trang và tìm kiếm/lọc")
    @GetMapping
    public ResponseEntity<Page<FireExtinguisherResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) MaintenanceStatus status,
            @RequestParam(required = false) String weight) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nextRechargeDate").ascending());

        return ResponseEntity.ok(service.getAll(keyword, zoneId, type, weight, status, pageable));
    }

    @Operation(summary = "Thêm mới bình chữa cháy")
    @PostMapping
    public ResponseEntity<FireExtinguisherResponse> create(@RequestBody FireExtinguisherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Ghi nhận nạp bình", description = "Có thể tùy chỉnh ngày nạp, ngày hết hạn và ghi chú")
    @PatchMapping("/{id}/recharge")
    public ResponseEntity<FireExtinguisherResponse> recharge(
            @PathVariable Long id,
            @RequestBody RechargeRequest request) {
        return ResponseEntity.ok(service.recharge(
                id,
                request.getRechargeDate(),
                request.getNextRechargeDate(),
                request.getNote()));
    }

    @Operation(summary = "Thống kê số lượng theo trạng thái")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("ok", service.countByStatus(MaintenanceStatus.OK));
        stats.put("warning", service.countByStatus(MaintenanceStatus.WARNING));
        stats.put("expired", service.countByStatus(MaintenanceStatus.EXPIRED));
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Thống kê nâng cao theo Khu vực và Loại bình")
    @GetMapping("/stats/advanced")
    public ResponseEntity<List<ZoneExtinguisherStatsResponse>> getAdvancedStats() {
        return ResponseEntity.ok(service.getAdvancedStats());
    }

    @Operation(summary = "Ghi nhận nạp bình hàng loạt theo Khu vực", description = "Áp dụng ngày nạp và ngày hết hạn cho tất cả bình trong 1 Khu")
    @PatchMapping("/zone/{zoneId}/recharge")
    public ResponseEntity<Map<String, String>> rechargeByZone(
            @PathVariable Long zoneId,
            @RequestBody RechargeRequest request) {
        service.rechargeByZone(zoneId, request);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Đã nạp thành công toàn bộ bình trong khu vực.");
        return ResponseEntity.ok(response);
    }
}