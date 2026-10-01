package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // VD: Hành lang lầu 1, Cửa chính, Phòng máy tính...

    // Mối quan hệ N-1: Nhiều vị trí thuộc về 1 Khu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    @JsonBackReference
    private Zone zone;
}