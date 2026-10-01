package com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory;

import com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory.dto.InventoryFileResponse;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory.dto.SaveLogsRequest;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.User;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Quản lý File Kiểm Kê", description = "API upload, chỉnh sửa và theo dõi lịch sử thay đổi file Excel/CSV kiểm kê tài sản")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserService userService;

    // ──────────────────────────────────────────────────────────────
    // FILE APIs
    // ──────────────────────────────────────────────────────────────

    /**
     * Upload file Excel/CSV mới.
     * Chỉ ADMIN và MANAGER mới có quyền upload.
     *
     * Request: multipart/form-data
     *   - file        : file .xlsx hoặc .csv
     *   - fileName    : (tùy chọn) tên hiển thị
     *   - departmentName : tên phòng ban
     */
    @Operation(summary = "Upload file kiểm kê mới",
            description = "Upload file Excel (.xlsx) hoặc CSV (.csv) lên Cloudinary và lưu metadata. Chỉ ADMIN/MANAGER.")
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InventoryFileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String fileName,
            @RequestParam String departmentName,
            Principal principal) throws IOException {

        String username = principal.getName(); // email từ JWT subject
        return ResponseEntity.ok(inventoryService.uploadFile(file, fileName, departmentName, username));
    }

    /**
     * Lấy danh sách file với phân trang, lọc theo phòng ban và từ khóa.
     *
     * Query params:
     *   - department : lọc theo tên phòng ban (optional)
     *   - keyword    : tìm kiếm trong fileName (optional)
     *   - page       : số trang (default 0)
     *   - size       : số item mỗi trang (default 20)
     */
    @Operation(summary = "Danh sách file kiểm kê",
            description = "Lấy danh sách file có phân trang, hỗ trợ lọc theo phòng ban và tìm kiếm theo tên file.")
    @GetMapping("/files")
    public ResponseEntity<Page<InventoryFileResponse>> getFiles(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
                inventoryService.searchFiles(department, keyword,
                        PageRequest.of(page, size, Sort.by("updatedAt").descending()))
        );
    }

    /**
     * Lấy thông tin chi tiết (metadata) của 1 file theo ID.
     * Frontend dùng để lấy cloudinaryUrl trước khi load file.
     */
    @Operation(summary = "Chi tiết file kiểm kê",
            description = "Lấy metadata của một file (tên, URL Cloudinary, phòng ban...) theo ID.")
    @GetMapping("/files/{id}")
    public ResponseEntity<InventoryFileResponse> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getFileById(id));
    }

    /**
     * Ghi đè nội dung file trên Cloudinary sau khi người dùng chỉnh sửa.
     * Frontend export file từ FortuneSheet → SheetJS → gửi Blob lên đây.
     *
     * Request: multipart/form-data
     *   - file : blob file xlsx xuất từ SheetJS
     */
    @Operation(summary = "Lưu thay đổi file (ghi đè)",
            description = "Nhận file Excel mới từ Frontend (sau khi chỉnh sửa) và ghi đè lên Cloudinary.")
    @PostMapping(value = "/files/{id}/overwrite", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InventoryFileResponse> overwriteFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {

        String username = principal.getName();
        return ResponseEntity.ok(inventoryService.overwriteFile(id, file, username));
    }

    /**
     * Cập nhật metadata file (tên hiển thị, phòng ban).
     * Không thay đổi nội dung file trên Cloudinary.
     */
    @Operation(summary = "Cập nhật thông tin file",
            description = "Cập nhật tên hiển thị và/hoặc tên phòng ban của file. Không ảnh hưởng nội dung file.")
    @PatchMapping("/files/{id}")
    public ResponseEntity<InventoryFileResponse> updateFileMeta(
            @PathVariable Long id,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String departmentName) {

        return ResponseEntity.ok(inventoryService.updateFileMeta(id, fileName, departmentName));
    }

    /**
     * Xóa file (xóa mềm trong DB + xóa thật trên Cloudinary).
     * Chỉ ADMIN mới có quyền xóa (được control bởi SecurityConfig).
     */
    @Operation(summary = "Xóa file kiểm kê",
            description = "Xóa file khỏi hệ thống: đánh dấu xóa trong DB và xóa file vật lý trên Cloudinary.")
    @DeleteMapping("/files/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) throws IOException {
        inventoryService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy danh sách tên phòng ban phân biệt.
     * Dùng để hiển thị dropdown filter trên Frontend.
     */
    @Operation(summary = "Danh sách phòng ban",
            description = "Trả về danh sách tên phòng ban phân biệt từ các file đã upload. Dùng cho dropdown filter.")
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        return ResponseEntity.ok(inventoryService.getAllDepartments());
    }

    // ──────────────────────────────────────────────────────────────
    // LOG APIs
    // ──────────────────────────────────────────────────────────────

    /**
     * Lưu batch thay đổi ô vào bảng inventory_logs.
     *
     * Frontend nên dùng debounce 500ms và gom nhiều ô thay đổi
     * thành 1 request thay vì gửi từng request cho mỗi lần gõ.
     *
     * Request body (JSON):
     * {
     *   "fileId": 1,
     *   "changes": [
     *     { "sheetName": "Sheet1", "cellAddress": "A5", "oldValue": "Cũ", "newValue": "Mới" },
     *     ...
     *   ]
     * }
     */
    @Operation(summary = "Lưu log thay đổi ô (batch)",
            description = "Nhận danh sách các thay đổi ô và lưu vào lịch sử. Frontend nên gom nhiều thay đổi và gửi 1 lần (debounce 500ms).")
    @PostMapping("/logs")
    public ResponseEntity<Void> saveLogs(
            @RequestBody SaveLogsRequest request,
            Principal principal) {

        String email = principal.getName();
        // Lấy fullName từ DB để lưu vào log cho tiện hiển thị
        User user = userService.findByEmail(email);
        String fullName = user != null ? user.getFullName() : email;

        inventoryService.saveLogs(request, email, fullName);
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy lịch sử thay đổi của 1 file, có phân trang và lọc theo người sửa.
     *
     * Query params:
     *   - changedBy : lọc theo email người sửa (optional)
     *   - page      : số trang (default 0)
     *   - size      : số item mỗi trang (default 50)
     */
    @Operation(summary = "Lịch sử thay đổi file",
            description = "Lấy danh sách log thay đổi từng ô của một file, có phân trang. Có thể lọc theo người sửa.")
    @GetMapping("/files/{fileId}/logs")
    public ResponseEntity<Page<InventoryLog>> getLogs(
            @PathVariable Long fileId,
            @RequestParam(required = false) String changedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(
                inventoryService.getLogs(fileId, changedBy,
                        PageRequest.of(page, size))
        );
    }
}
