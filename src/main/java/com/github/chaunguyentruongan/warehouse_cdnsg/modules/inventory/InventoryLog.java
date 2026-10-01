package com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Bảng lưu lịch sử thay đổi từng ô dữ liệu trong file Excel/CSV.
 * Mỗi bản ghi = 1 lần sửa 1 ô bởi 1 người.
 */
@Entity
@Table(name = "inventory_logs", indexes = {
        @Index(name = "idx_inv_log_file_id", columnList = "file_id"),
        @Index(name = "idx_inv_log_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** File liên quan */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private InventoryFile file;

    /**
     * Tên sheet chứa ô thay đổi.
     * File Excel có thể có nhiều sheet - cần ghi rõ.
     */
    @Column(nullable = false)
    private String sheetName;

    /** Địa chỉ ô dạng chuẩn Excel: A5, B10, Z100... */
    @Column(nullable = false)
    private String cellAddress;

    /** Giá trị ô trước khi sửa (null nếu là lần nhập đầu tiên) */
    @Column(columnDefinition = "TEXT")
    private String oldValue;

    /** Giá trị ô sau khi sửa */
    @Column(columnDefinition = "TEXT")
    private String newValue;

    /** Username của người thực hiện thay đổi (lấy từ JWT) */
    @Column(nullable = false)
    private String changedBy;

    /** Tên đầy đủ - lưu để hiển thị nhanh, tránh join User */
    private String changedByFullName;

    /** Thời điểm thay đổi (server-side, không tin client) */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
