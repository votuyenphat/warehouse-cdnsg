package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FireExtinguisherResponse {
    private Long id;

    // THAY ĐỔI: Trả về thông tin chi tiết của Vị trí và Khu vực
    private Long locationId;
    private String locationName;
    private Long zoneId;
    private String zoneName;

    private String type;
    private String weight;
    private Integer quantity;

    // BỔ SUNG
    private String unit;
    private String note;

    private LocalDate lastRechargeDate;
    private LocalDate nextRechargeDate;
    private String status; // OK, WARNING, EXPIRED
}