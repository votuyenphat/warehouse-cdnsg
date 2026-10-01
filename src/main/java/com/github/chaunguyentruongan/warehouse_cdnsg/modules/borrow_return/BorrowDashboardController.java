package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/borrow-dashboard")
@RequiredArgsConstructor
@Tag(name = "Borrow Dashboard", description = "Các API thống kê, KPI cho hệ thống Mượn/Trả")
public class BorrowDashboardController {

    private final BorrowDashboardService dashboardService;

    @Operation(summary = "Lấy các chỉ số KPI nhanh")
    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpis());
    }

    @Operation(summary = "Lấy các danh sách xử lý nhanh (top chờ duyệt, quá hạn, sắp hết hàng)")
    @GetMapping("/quick-lists")
    public ResponseEntity<Map<String, Object>> getQuickLists() {
        return ResponseEntity.ok(dashboardService.getQuickLists());
    }
}
