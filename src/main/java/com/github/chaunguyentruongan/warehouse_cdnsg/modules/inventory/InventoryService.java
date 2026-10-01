package com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory.dto.InventoryFileResponse;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory.dto.SaveLogsRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final String CLOUDINARY_FOLDER = "inventory_files";

    private final InventoryFileRepository fileRepository;
    private final InventoryLogRepository logRepository;
    private final Cloudinary cloudinary;

    // ──────────────────────────────────────────────────────────────
    // FILE OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Upload file Excel/CSV mới lên Cloudinary và lưu metadata vào DB.
     *
     * @param file           file gửi lên từ multipart/form-data
     * @param customFileName tên hiển thị tùy chỉnh; nếu null thì dùng tên gốc
     * @param departmentName tên phòng ban
     * @param username       username người upload (lấy từ JWT)
     * @return metadata của file vừa upload
     */
    @Transactional
    public InventoryFileResponse uploadFile(
            MultipartFile file,
            String customFileName,
            String departmentName,
            String username) throws IOException {

        // File Excel (.xlsx/.csv) phải dùng resource_type = "raw",
        // không dùng "auto" vì Cloudinary sẽ báo lỗi với file binary không phải ảnh/video.
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "folder", CLOUDINARY_FOLDER,
                        "use_filename", true,
                        "unique_filename", true
                )
        );

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase()
                : "xlsx";

        LocalDateTime now = LocalDateTime.now();
        InventoryFile inventoryFile = InventoryFile.builder()
                .fileName(customFileName != null && !customFileName.isBlank() ? customFileName : originalName)
                .departmentName(departmentName)
                .cloudinaryUrl(uploadResult.get("secure_url").toString())
                .publicId(uploadResult.get("public_id").toString())
                .fileSize(file.getSize())
                .fileType(ext)
                .createdBy(username)
                .createdAt(now)
                .updatedAt(now)
                .deleted(false)
                .build();

        return toResponse(fileRepository.save(inventoryFile));
    }

    /**
     * Ghi đè nội dung file trên Cloudinary sau khi người dùng chỉnh sửa và nhấn "Lưu".
     * Cloudinary sẽ giữ nguyên publicId nhưng cập nhật nội dung file.
     *
     * @param fileId  ID file trong DB
     * @param newFile blob mới từ Frontend (sau khi SheetJS export)
     * @param username người thực hiện lưu
     * @return metadata sau khi cập nhật
     */
    @Transactional
    public InventoryFileResponse overwriteFile(
            Long fileId,
            MultipartFile newFile,
            String username) throws IOException {

        InventoryFile inventoryFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy file với id: " + fileId));

        if (inventoryFile.isDeleted()) {
            throw new RuntimeException("File đã bị xóa");
        }

        // Ghi đè lên cùng publicId - Cloudinary tạo version mới tự động
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                newFile.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw",
                        "public_id", inventoryFile.getPublicId(),
                        "overwrite", true,
                        "invalidate", true  // Xóa cache CDN ngay lập tức
                )
        );

        inventoryFile.setCloudinaryUrl(uploadResult.get("secure_url").toString());
        inventoryFile.setFileSize(newFile.getSize());
        inventoryFile.setUpdatedAt(LocalDateTime.now());

        return toResponse(fileRepository.save(inventoryFile));
    }

    /**
     * Lấy thông tin metadata của 1 file theo ID.
     */
    public InventoryFileResponse getFileById(Long fileId) {
        InventoryFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy file với id: " + fileId));
        if (file.isDeleted()) {
            throw new RuntimeException("File đã bị xóa");
        }
        return toResponse(file);
    }

    /**
     * Tìm kiếm và lọc danh sách file theo phòng ban và từ khóa.
     */
    public Page<InventoryFileResponse> searchFiles(String department, String keyword, Pageable pageable) {
        return fileRepository.searchFiles(department, keyword, pageable).map(this::toResponse);
    }

    /**
     * Lấy danh sách tên phòng ban phân biệt để hiển thị dropdown filter trên Frontend.
     */
    public List<String> getAllDepartments() {
        return fileRepository.findAllDistinctDepartments();
    }

    /**
     * Xóa mềm file: đánh dấu deleted = true trong DB và xóa thật trên Cloudinary.
     */
    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        InventoryFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy file với id: " + fileId));

        // Xóa file vật lý trên Cloudinary
        cloudinary.uploader().destroy(
                file.getPublicId(),
                ObjectUtils.asMap("resource_type", "raw")
        );

        // Xóa mềm trong DB
        file.setDeleted(true);
        fileRepository.save(file);
    }

    /**
     * Cập nhật thông tin metadata file (tên, phòng ban).
     * Không thay đổi file vật lý trên Cloudinary.
     */
    @Transactional
    public InventoryFileResponse updateFileMeta(Long fileId, String fileName, String departmentName) {
        InventoryFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy file với id: " + fileId));

        if (fileName != null && !fileName.isBlank()) {
            file.setFileName(fileName);
        }
        if (departmentName != null && !departmentName.isBlank()) {
            file.setDepartmentName(departmentName);
        }
        file.setUpdatedAt(LocalDateTime.now());
        return toResponse(fileRepository.save(file));
    }

    // ──────────────────────────────────────────────────────────────
    // LOG OPERATIONS
    // ──────────────────────────────────────────────────────────────

    /**
     * Lưu batch thay đổi ô vào bảng inventory_logs.
     * Frontend nên gom nhiều thay đổi lại (debounce 500ms) rồi gửi 1 lần thay vì gửi từng request.
     *
     * @param request    danh sách các ô thay đổi kèm file ID
     * @param username   username người sửa (từ JWT)
     * @param fullName   tên đầy đủ người sửa (từ JWT)
     */
    @Transactional
    public void saveLogs(SaveLogsRequest request, String username, String fullName) {
        InventoryFile file = fileRepository.findById(request.getFileId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy file với id: " + request.getFileId()));

        if (request.getChanges() == null || request.getChanges().isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<InventoryLog> logs = request.getChanges().stream()
                .map(change -> InventoryLog.builder()
                        .file(file)
                        .sheetName(change.getSheetName() != null ? change.getSheetName() : "Sheet1")
                        .cellAddress(change.getCellAddress())
                        .oldValue(change.getOldValue())
                        .newValue(change.getNewValue())
                        .changedBy(username)
                        .changedByFullName(fullName)
                        .createdAt(now)
                        .build())
                .toList();

        logRepository.saveAll(logs);
    }

    /**
     * Lấy lịch sử thay đổi của 1 file, có phân trang.
     */
    public Page<InventoryLog> getLogs(Long fileId, String changedBy, Pageable pageable) {
        if (changedBy != null && !changedBy.isBlank()) {
            return logRepository.findByFileIdAndChangedBy(fileId, changedBy, pageable);
        }
        return logRepository.findByFileIdOrderByCreatedAtDesc(fileId, pageable);
    }

    // ──────────────────────────────────────────────────────────────
    // MAPPER
    // ──────────────────────────────────────────────────────────────

    private InventoryFileResponse toResponse(InventoryFile f) {
        return InventoryFileResponse.builder()
                .id(f.getId())
                .fileName(f.getFileName())
                .departmentName(f.getDepartmentName())
                .cloudinaryUrl(f.getCloudinaryUrl())
                .fileSize(f.getFileSize())
                .fileType(f.getFileType())
                .createdBy(f.getCreatedBy())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
