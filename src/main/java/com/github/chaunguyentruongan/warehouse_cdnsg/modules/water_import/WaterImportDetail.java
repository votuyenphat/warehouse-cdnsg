package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "water_import_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterImportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "water_import_id", nullable = false)
    @JsonBackReference
    private WaterImport waterImport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "water_zone_id", nullable = false)
    private WaterZone zone;

    @Column(nullable = false)
    private Integer quantity; // Số lượng ở khu vực này
}
