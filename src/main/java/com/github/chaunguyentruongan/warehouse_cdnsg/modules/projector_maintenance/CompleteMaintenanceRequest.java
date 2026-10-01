package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.ProjectorStatus;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CompleteMaintenanceRequest {
    private LocalDate completionDate;

    @JsonProperty("items") // Ánh xạ tên "items" từ React gửi sang biến "results"
    private List<MaintenanceResult> results;

    @Getter
    @Setter
    public static class MaintenanceResult {
        @JsonProperty("id") // Ánh xạ id của React
        private Long projectorId;

        @JsonProperty("nextStatus") // Ánh xạ trạng thái mà người dùng chọn
        private ProjectorStatus status;
    }
}