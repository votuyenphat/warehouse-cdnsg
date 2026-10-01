package com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bảng lưu metadata của các file Excel/CSV được upload lên Cloudinary.
 * Mỗi file thuộc về một phòng ban (department) cụ thể.
 */
@Entity
@Table(name = "inventory_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tên hiển thị (VD: "1 - Phòng TC-HC-01-2026") */
    @Column(nullable = false)
    private String fileName;

    /** Tên phòng ban sở hữu file */
    @Column(nullable = false)
    private String departmentName;

    /** Public URL trả về từ Cloudinary để frontend fetch */
    @Column(nullable = false, length = 1024)
    private String cloudinaryUrl;

    /**
     * Public ID trên Cloudinary - dùng để ghi đè (overwrite) hoặc xóa file.
     * Quan trọng: bắt buộc phải lưu, không chỉ lưu URL.
     */
    @Column(nullable = false)
    private String publicId;

    /** Kích thước file (bytes) */
    private Long fileSize;

    /** Loại file: xlsx / csv */
    private String fileType;

    /** Người tạo (username lấy từ JWT) */
    @Column(nullable = false)
    private String createdBy;

    /** Thời điểm tạo lần đầu */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Lần cuối cập nhật nội dung */
    private LocalDateTime updatedAt;

    /** Cờ xóa mềm - không xóa thật khỏi DB */
    @Builder.Default
    private boolean deleted = false;
}
