package com.github.chaunguyentruongan.warehouse_cdnsg.modules.dashboards;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.MaterialRepository;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt.ExportReceiptRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Thống kê Tổng quan", description = "Các API cung cấp số liệu cho màn hình Dashboard")
public class DashboardController {

    private final MaterialRepository materialRepository;
    private final ExportReceiptRepository exportReceiptRepository;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummaryStats() {
        Map<String, Object> stats = new HashMap<>();

        // Tổng số loại vật tư đang quản lý
        long totalMaterials = materialRepository.count();
        stats.put("totalMaterials", totalMaterials);

        // Số vật tư sắp hết (tồn kho < 5)
        long lowStockAlerts = materialRepository.countLowStock();
        stats.put("lowStockAlerts", lowStockAlerts);

        // Tổng số lượng tồn kho của toàn bộ vật tư
        long totalInventory = materialRepository.sumTotalInventory();
        stats.put("totalInventory", totalInventory);

        // Số phiếu xuất trong tháng hiện tại
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        long totalExportsThisMonth = exportReceiptRepository.countByExportDateBetween(startOfMonth, endOfMonth);
        stats.put("totalExportsThisMonth", totalExportsThisMonth);

        return ResponseEntity.ok(stats);
    }
}