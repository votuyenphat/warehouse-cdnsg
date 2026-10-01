package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_loan.ProjectorLoan;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance.ProjectorMaintenanceDetail;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Table(name = "projector")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Projector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Tên dòng máy (VD: Panasonic PT-LB383, Sony VPL-EX430)

    // Mỗi máy chiếu nên có một mã Serial duy nhất để phân biệt
    @Column(unique = true, nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectorStatus status;

    // Liên kết 1-N tới lịch sử Bảo trì
    @OneToMany(mappedBy = "projector", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("projector")
    @Builder.Default
    private List<ProjectorMaintenanceDetail> maintenances = new ArrayList<>();

    // Liên kết 1-N tới lịch sử Mượn/Trả
    @OneToMany(mappedBy = "projector", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("projector")
    @Builder.Default
    private List<ProjectorLoan> loans = new ArrayList<>();
}