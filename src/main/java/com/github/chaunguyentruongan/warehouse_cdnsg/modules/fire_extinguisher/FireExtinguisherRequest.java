package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FireExtinguisherRequest {
    private Long locationId; // THAY ĐỔI: Nhận ID của vị trí thay vì chuỗi
    private String type;
    private String weight;
    private Integer quantity;

    // BỔ SUNG
    private String unit;
    private String note;
}