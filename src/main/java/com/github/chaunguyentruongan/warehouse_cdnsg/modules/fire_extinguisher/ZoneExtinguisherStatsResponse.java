package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneExtinguisherStatsResponse {
    private String zoneName; // Tên khu vực
    private String type; // Loại bình (VD: Bột BC, CO2)
    private Long totalQuantity; // Tổng số lượng
    private LocalDate maxLastRechargeDate; // Ngày nạp gần nhất trong nhóm
    private LocalDate minNextRechargeDate; // Ngày hết hạn sớm nhất trong nhóm
}