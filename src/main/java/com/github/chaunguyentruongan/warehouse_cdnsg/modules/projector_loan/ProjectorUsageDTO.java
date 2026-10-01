package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_loan;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectorUsageDTO {
    private Long projectorId;
    private String projectorName;
    private String serialNumber;
    private Long totalUsageSeconds; // Tổng thời gian tính bằng giây
}