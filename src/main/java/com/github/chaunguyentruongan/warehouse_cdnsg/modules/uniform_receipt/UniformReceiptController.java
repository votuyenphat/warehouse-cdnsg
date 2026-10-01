package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_receipt;

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
@RequestMapping("/api/uniform-receipts")
@RequiredArgsConstructor
@Tag(name = "Quản lý Xuất/Cấp phát Đồng phục", description = "Các API thêm, sửa, xóa, tìm kiếm phiếu cấp phát đồng phục cho nhân viên/khách hàng")
public class UniformReceiptController {

    private final UniformReceiptService receiptService;

    @Operation(summary = "Tạo mới phiếu xuất (cấp phát)", description = "Ghi nhận xuất kho đồng phục. Tổng số lượng (totalQuantity) sẽ được tính tự động. Tồn kho sẽ bị trừ đi.")
    @PostMapping
    public ResponseEntity<UniformReceipt> create(@RequestBody UniformReceiptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(receiptService.create(request));
    }

    @Operation(summary = "Lấy danh sách phiếu xuất", description = "Hỗ trợ phân trang và tìm kiếm theo khoảng thời gian, tên người nhận (cusName).")
    @GetMapping
    public ResponseEntity<Page<UniformReceipt>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String cusName) {

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UniformReceipt> result = receiptService.getAll(fromDate, toDate, cusName, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Lấy chi tiết phiếu xuất", description = "Tìm kiếm thông tin phiếu xuất đồng phục dựa trên ID")
    @GetMapping("/{id}")
    public ResponseEntity<UniformReceipt> getById(@PathVariable Long id) {
        return ResponseEntity.ok(receiptService.findById(id));
    }

    @Operation(summary = "Cập nhật phiếu xuất", description = "Cập nhật thông tin phiếu cấp phát. Tồn kho sẽ tự động được bù trừ chuẩn xác.")
    @PutMapping("/{id}")
    public ResponseEntity<UniformReceipt> update(@PathVariable Long id, @RequestBody UniformReceiptRequest request) {
        return ResponseEntity.ok(receiptService.update(id, request));
    }

    @Operation(summary = "Xóa phiếu xuất", description = "Xóa phiếu xuất theo ID. Số lượng đồng phục đã xuất sẽ được cộng trả lại vào tồn kho.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        receiptService.delete(id);
        return ResponseEntity.noContent().build();
    }
}