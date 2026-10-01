package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE borrow_item SET deleted = true WHERE id=?")
@Where(clause = "deleted = false")
public class BorrowItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category; // Thiết bị IT, Thiết bị văn phòng, Vật tư khác

    @Column(columnDefinition = "TEXT")
    private String description;

    private String unit;

    private int totalQuantity; // Tổng nhập kho
    private int availableQuantity; // Số lượng khả dụng
    private int brokenLostQuantity; // Số lượng hỏng/mất
    private int pendingQuantity; // Số lượng đang chờ duyệt
    private int borrowedQuantity; // Số lượng đang được mượn

    private LocalDateTime createdAt;
    private String createdBy;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Transient
    public String getStatus() {
        if (availableQuantity > 0) return "AVAILABLE";
        if (pendingQuantity > 0) return "PENDING";
        if (borrowedQuantity > 0) return "BORROWED";
        if (brokenLostQuantity > 0) return "BROKEN/LOST";
        return "OUT_OF_STOCK";
    }
}
