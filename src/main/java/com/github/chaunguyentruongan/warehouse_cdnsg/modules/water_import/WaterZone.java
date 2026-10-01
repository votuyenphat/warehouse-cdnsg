package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "water_zone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // VD: Kho nước A, Khu hành chính...

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả thêm
}
