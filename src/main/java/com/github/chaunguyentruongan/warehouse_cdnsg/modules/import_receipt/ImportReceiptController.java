package com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/import-receipt")
@RequiredArgsConstructor
@Tag(name = "Quản lý Nhập Kho", description = "Các API thêm, sửa, xóa, tìm kiếm phiếu nhập hàng")
public class ImportReceiptController {

    private final ImportReceiptService importReceiptService;

    // CREATE
    @PostMapping
    public ResponseEntity<ImportReceipt> createImport(@RequestBody ImportReceiptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importReceiptService.create(request));
    }

    // GET ALL - Phân trang, Tìm kiếm, Lọc
    @GetMapping
    public ResponseEntity<Page<ImportReceipt>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String keyword) { // ĐỔI TÊN BIẾN THÀNH keyword

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ImportReceipt> result = importReceiptService.getAll(fromDate, toDate, keyword, pageable);
        return ResponseEntity.ok(result);
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ImportReceipt> getById(@PathVariable Long id) {
        return ResponseEntity.ok(importReceiptService.findById(id));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ImportReceipt> updateImport(@PathVariable Long id,
            @RequestBody ImportReceiptRequest request) {
        return ResponseEntity.ok(importReceiptService.update(id, request));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImport(@PathVariable Long id) {
        importReceiptService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/report")
    public ResponseEntity<List<DailyImportReportDTO>> getImportReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) Integer quantity) {

        List<DailyImportReportDTO> report = importReceiptService.getDailyImportReport(
                fromDate, toDate, materialName, note, quantity);

        return ResponseEntity.ok(report);
    }
}