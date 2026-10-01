package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.ProjectorStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class MaintenanceRequest {
    private Long projectorId;
    private LocalDate maintenanceDate;
    private String description;
    private String technician;
    private Double cost;
    private ProjectorStatus updateProjectorStatusTo; // Trạng thái sau khi bảo trì (AVAILABLE hoặc BROKEN)
}