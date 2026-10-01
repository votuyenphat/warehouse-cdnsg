package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Tag(name = "Quản lý Vật tư", description = "Các API thêm, sửa, xóa, lấy danh sách thông tin vật tư (hàng hóa)")
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "Tạo mới vật tư", description = "Thêm một loại vật tư mới vào danh mục. Tồn kho mặc định ban đầu sẽ là 0.")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody MaterialRequestCreate request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materialService.create(request));
    }

    @Operation(summary = "Lấy chi tiết vật tư", description = "Truy vấn thông tin chi tiết của một vật tư cụ thể dựa trên ID.")
    @GetMapping
    public ResponseEntity<?> get(@RequestParam Long id) {
        return ResponseEntity.ok(materialService.findById(id));
    }

    @Operation(summary = "Lấy toàn bộ vật tư (có phân trang)", description = "Trả về danh sách vật tư kèm thông tin phân trang.")
    @GetMapping("/all")
    public ResponseEntity<Page<Material>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) { // <--- THÊM PARAM NÀY

        Sort sort = direction.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Truyền thêm status vào service
        return ResponseEntity.ok(materialService.findAll(keyword, status, pageable));
    }

    @Operation(summary = "Cập nhật vật tư", description = "Chỉnh sửa thông tin (tên, đơn vị tính) của vật tư đang tồn tại theo ID.")
    @PutMapping
    public ResponseEntity<?> update(@RequestParam Long id, @RequestBody MaterialRequestCreate requestCreate) {
        return ResponseEntity.status(HttpStatus.OK).body(materialService.update(id, requestCreate));
    }

    @Operation(summary = "Xóa vật tư", description = "Xóa hoàn toàn một vật tư khỏi hệ thống dựa trên ID.")
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Long id) {
        // Đã fix lỗi logic: Gọi service để thực sự xóa dưới Database
        materialService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Delete materials id: " + id);
    }

    @Operation(summary = "Thống kê vật tư", description = "Lấy các chỉ số tổng quan cho Dashboard")
    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getStats() {
        return ResponseEntity.ok(materialService.getMaterialStats());
    }

    @Operation(summary = "Lấy top 10 vật tư xuất nhiều nhất", description = "Trả về danh sách 10 vật tư có tổng số lượng xuất kho nhiều nhất (chỉ tính phiếu xuất COMPLETED)")
    @GetMapping("/top-exported")
    public ResponseEntity<List<TopMaterialDTO>> getTop10ExportedMaterials() {
        return ResponseEntity.ok(materialService.getTop10MostUsedMaterials());
    }

    @Operation(summary = "Lấy thống kê giao dịch gần nhất", description = "Trả về danh sách 10 vật tư đã nhập và 10 vật tư đã xuất gần đây nhất")
    @GetMapping("/recent-activities")
    public ResponseEntity<RecentActivitiesResponse> getRecentActivities() {
        return ResponseEntity.ok(materialService.getRecentActivities());
    }

    @Operation(summary = "Lấy lịch sử giao dịch của vật tư", description = "Trả về lịch sử nhập xuất của một vật tư cụ thể dựa trên ID")
    @GetMapping("/{id}/history")
    public ResponseEntity<MaterialDetailResponseDTO> getMaterialHistory(@PathVariable Long id) {
        MaterialDetailResponseDTO response = materialService.getMaterialDetailsWithHistory(id);
        return ResponseEntity.ok(response);
    }
}