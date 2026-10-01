package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_loan;

import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.ProjectorStatus;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/projector-loans")
@RequiredArgsConstructor
@Tag(name = "Quản lý Mượn/Trả Máy chiếu", description = "Nghiệp vụ mượn, trả và xem lịch sử")
public class ProjectorLoanController {

    private final ProjectorLoanService loanService;

    @Operation(summary = "Mượn máy chiếu", description = "Hỗ trợ mượn nhiều máy cùng lúc")
    @PostMapping("/borrow")
    public ResponseEntity<List<ProjectorLoan>> borrowProjector(@RequestBody ProjectorLoanRequest request) { // SỬA: Trả
                                                                                                            // về List
        return ResponseEntity.ok(loanService.borrowProjector(request));
    }

    @Operation(summary = "Trả máy chiếu", description = "Có thể set trạng thái máy chiếu sau khi trả (VD: trả về bị hỏng -> BROKEN)")
    @PostMapping("/{loanId}/return")
    public ResponseEntity<ProjectorLoan> returnProjector(
            @PathVariable Long loanId,
            @RequestParam(required = false, defaultValue = "") String returnNote,
            @RequestParam(required = false) ProjectorStatus nextStatus) {
        return ResponseEntity.ok(loanService.returnProjector(loanId, returnNote, nextStatus));
    }

    @Operation(summary = "Cập nhật thông tin phiếu mượn")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectorLoan> updateLoan(
            @PathVariable Long id,
            @RequestBody ProjectorLoanRequest request) {
        return ResponseEntity.ok(loanService.updateLoan(id, request));
    }

    @Operation(summary = "Lấy danh sách mượn/trả có lọc")
    @GetMapping
    public ResponseEntity<Page<ProjectorLoan>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LoanStatus status) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(loanService.getAllWithFilter(keyword, status, pageable));
    }

    @Operation(summary = "Lấy lịch sử mượn của 1 máy chiếu cụ thể")
    @GetMapping("/projector/{projectorId}")
    public ResponseEntity<List<ProjectorLoan>> getHistoryByProjector(@PathVariable Long projectorId) {
        return ResponseEntity.ok(loanService.getHistoryByProjectorId(projectorId));
    }

    @Operation(summary = "Xóa lịch sử mượn của 1 máy chiếu cụ thể")
    @DeleteMapping("/{projectorId}")
    public ResponseEntity<Void> deleteHistoryByProjector(@PathVariable Long projectorId) {
        loanService.deleteByProjectorId(projectorId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Báo cáo thống kê thời gian sử dụng", description = "Tính tổng thời gian (giây) máy đã được mượn dựa trên các phiếu đã hoàn tất")
    @GetMapping("/statistics/usage")
    public ResponseEntity<List<ProjectorUsageDTO>> getUsageReport() {
        return ResponseEntity.ok(loanService.getProjectorUsageReport());
    }

}