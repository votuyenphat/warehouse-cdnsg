package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.Projector;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projector_maintenance_detail") // Đổi tên bảng để phản ánh đúng vai trò
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProjectorMaintenanceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnoreProperties("details")
    private MaintenanceTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projector_id", nullable = false)
    @JsonIgnoreProperties({ "loans", "maintenances" }) // THAY ĐỔI: Sửa lỗi @JsonBackReference
    private Projector projector;

    private String description; // Tình trạng/Nội dung sửa cho riêng máy này
    private Double cost;
}