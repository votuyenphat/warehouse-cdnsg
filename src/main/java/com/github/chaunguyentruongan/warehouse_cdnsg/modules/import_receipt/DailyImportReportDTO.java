package com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyImportReportDTO {
    private LocalDate importDate;
    private int totalQuantityOfDay; // Tổng số lượng nhập trong ngày
    private List<ImportReportDetailDTO> details; // Danh sách chi tiết
}