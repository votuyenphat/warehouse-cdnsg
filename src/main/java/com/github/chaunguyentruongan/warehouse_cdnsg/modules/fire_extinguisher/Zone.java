package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // VD: Khu A, Kho hàng số 1...

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả thêm về khu vực này
}