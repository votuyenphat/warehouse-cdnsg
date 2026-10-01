package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import jakarta.persistence.*;
import lombok.*;

import java.text.Normalizer;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "fire_extinguisher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FireExtinguisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // THAY ĐỔI: Chuyển từ String sang liên kết với bảng Location
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    @JsonManagedReference
    private Location location;

    @Column(nullable = false)
    private String type; // Loại bình (VD: Bột BC, CO2, Foam...)

    private String weight; // Trọng lượng (VD: 4kg, 8kg...)

    private Integer quantity; // Số lượng tại vị trí đó

    // BỔ SUNG: Đơn vị tính
    @Column(length = 50)
    private String unit; // VD: Bình, Quả...

    // BỔ SUNG: Ghi chú
    @Column(columnDefinition = "TEXT")
    private String note;

    private LocalDate lastRechargeDate; // Ngày nạp gần nhất

    private LocalDate nextRechargeDate; // Ngày nạp tiếp theo (Dự kiến)

    // Trạng thái để nhắc nhở (VD: OK, WARNING, EXPIRED)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus status;

    public static String normalize(String str) {
        if (str == null)
            return null;
        String temp = Normalizer.normalize(str, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{M}", "").toLowerCase();
    }
}

enum MaintenanceStatus {
    OK, WARNING, EXPIRED
}