package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow-return")
@RequiredArgsConstructor
@Tag(name = "Borrow & Return Management", description = "Các API quản lý luồng mượn và trả vật tư bằng mã QR")
public class BorrowReturnController {

    private final BorrowReturnService service;

    @Operation(summary = "1. Tạo phiếu mượn vật tư", description = "Người dùng tạo phiếu mượn mới. Trạng thái mặc định là PENDING và gửi email thông báo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo phiếu thành công")
    })
    @PostMapping("/create")
    public ResponseEntity<BorrowTicket> createTicket(@RequestBody BorrowRequestDTO request) {
        return ResponseEntity.ok(service.createTicket(request));
    }

    @Operation(summary = "2. Admin duyệt đơn", description = "Admin duyệt phiếu mượn, trừ tồn kho và chuyển trạng thái sang BORROWED. Gửi email kèm mã QR cho người mượn.")
    @PutMapping("/{id}/approve")
    public ResponseEntity<BorrowTicket> approveTicket(@PathVariable Long id) {
        return ResponseEntity.ok(service.approveTicket(id));
    }

    @Operation(summary = "3. Admin từ chối đơn", description = "Admin từ chối phiếu mượn, kèm lý do và gửi email.")
    @PutMapping("/{id}/reject")
    public ResponseEntity<BorrowTicket> rejectTicket(@PathVariable Long id, @RequestParam String reason) {
        return ResponseEntity.ok(service.rejectTicket(id, reason));
    }

    @Operation(summary = "4. Lấy thông tin phiếu trả qua mã QR", description = "Được gọi khi thủ kho quét mã QR trả đồ. API trả về chi tiết phiếu để hiển thị lên màn hình kiểm tra trước khi xác nhận.")
    @Parameter(name = "returnCode", description = "Mã trả đồ (được gửi trong email của người mượn)", required = true)
    @GetMapping("/scan-return/{returnCode}")
    public ResponseEntity<BorrowTicket> getReturnTicketInfo(@PathVariable String returnCode) {
        BorrowTicket ticket = service.getTicketByReturnCode(returnCode);
        return ResponseEntity.ok(ticket);
    }

    @Operation(summary = "5. Thủ kho xác nhận trả đồ (QR)", description = "Thủ kho xác nhận qua mã QR code. Hỗ trợ ghi chú từng vật phẩm.")
    @PostMapping("/confirm-return")
    public ResponseEntity<BorrowTicket> confirmReturn(@RequestBody ConfirmReturnDTO request) {
        return ResponseEntity.ok(service.confirmReturn(request));
    }

    @Operation(summary = "5.1. Thủ kho xác nhận trả đồ (Thủ công)", description = "Thủ kho xác nhận trả đồ không qua mã QR, sử dụng trực tiếp ID phiếu.")
    @PostMapping("/{id}/return-manual")
    public ResponseEntity<BorrowTicket> returnManual(@PathVariable Long id, @RequestBody ConfirmReturnDTO request) {
        request.setTicketId(id);
        return ResponseEntity.ok(service.confirmReturn(request));
    }

    @Operation(summary = "6. Admin xác nhận đã nhận đủ đồ bị thiếu", description = "Được gọi khi người dùng mang vật tư bị thiếu/hỏng đến trả bù. Hệ thống sẽ đổi từ INCOMPLETE sang RETURNED và hoàn lại tồn kho.")
    @PutMapping("/resolve-incomplete/{id}")
    public ResponseEntity<BorrowTicket> resolveIncompleteReturn(@PathVariable Long id) {
        return ResponseEntity.ok(service.resolveIncomplete(id));
    }

    @Operation(summary = "7. Lấy danh sách phiếu mượn trả", description = "Lấy toàn bộ danh sách phiếu có phân trang và lọc.")
    @GetMapping("/all")
    public ResponseEntity<Page<BorrowTicket>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TicketStatus status) {

        return ResponseEntity.ok(
                service.findAll(keyword, status, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @Operation(summary = "8. Cập nhật thông tin phiếu", description = "Admin sửa thông tin (Tên, phòng ban, ghi chú, trạng thái).")
    @PutMapping("/{id}")
    public ResponseEntity<BorrowTicket> updateTicket(@PathVariable Long id,
            @RequestBody UpdateBorrowTicketDTO request) {
        return ResponseEntity.ok(service.updateTicket(id, request));
    }

    @Operation(summary = "9. Xóa phiếu mượn", description = "Admin xóa mềm phiếu mượn.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        service.deleteTicket(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "10. Gửi email thủ công", description = "Quản lý soạn nội dung gửi trực tiếp đến cá nhân hoặc nhóm người dùng.")
    @PostMapping("/send-email")
    public ResponseEntity<Void> sendManualEmail(@RequestBody ManualEmailRequest request) {
        service.sendManualEmail(request);
        return ResponseEntity.ok().build();
    }
}