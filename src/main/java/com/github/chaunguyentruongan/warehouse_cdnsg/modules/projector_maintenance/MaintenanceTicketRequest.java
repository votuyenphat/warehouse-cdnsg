package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MaintenanceTicketRequest {
    private String ticketCode;
    private LocalDate startDate;
    private String technician;
    private String generalNote;

    // Nhận mảng [1, 2, 3] từ Frontend gửi lên
    private List<Long> projectorIds;

    private List<MaintenanceDetailRequest> details;
}