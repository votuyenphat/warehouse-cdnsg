package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenanceDetailRequest {
    private Long projectorId; // ID của máy chiếu cần bảo trì
    private String description; // Tình trạng lỗi hoặc nội dung cần làm (VD: "Thay bóng đèn", "Vệ sinh")
    private Double cost; // Chi phí dự kiến/thực tế cho riêng máy này (tùy chọn)
}