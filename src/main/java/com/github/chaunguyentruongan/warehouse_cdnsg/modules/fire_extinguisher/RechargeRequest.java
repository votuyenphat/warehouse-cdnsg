package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RechargeRequest {
    private LocalDate rechargeDate; // Ngày nạp thực tế (Bắt buộc)

    private LocalDate nextRechargeDate; // Ngày hết hạn (Tùy chọn, nếu null hệ thống tự cộng 6 tháng)

    private String note; // Ghi chú thêm (VD: Nạp tại cơ sở A, thay ti van...)
}