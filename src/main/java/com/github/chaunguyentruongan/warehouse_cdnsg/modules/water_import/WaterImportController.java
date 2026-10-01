package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/water-imports")
@RequiredArgsConstructor
@Tag(name = "Quản lý Nhập nước về", description = "Các API theo dõi nước nhập về, số lượng, ngày sản xuất, vị trí lưu trữ và vỏ bình")
public class WaterImportController {

    private final WaterImportService service;

    @Operation(summary = "Ghi nhận phiếu nhập nước mới")
    @PostMapping
    public ResponseEntity<WaterImportResponse> create(@RequestBody WaterImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Cập nhật phiếu nhập nước")
    @PutMapping("/{id}")
    public ResponseEntity<WaterImportResponse> update(@PathVariable Long id, @RequestBody WaterImportRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Lấy chi tiết phiếu nhập nước")
    @GetMapping("/{id}")
    public ResponseEntity<WaterImportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Xóa phiếu nhập nước")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Danh sách phiếu nhập nước có phân trang và lọc theo ngày")
    @GetMapping
    public ResponseEntity<Page<WaterImportResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("importDate").descending().and(Sort.by("id").descending()));
        return ResponseEntity.ok(service.getAll(fromDate, toDate, pageable));
    }

    @Operation(summary = "Báo cáo thống kê nhập nước từ ngày đến ngày")
    @GetMapping("/report")
    public ResponseEntity<WaterImportReportResponse> getReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        return ResponseEntity.ok(service.getReport(fromDate, toDate));
    }
}
