package com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportReportFlatDTO {
    private LocalDate exportDate;
    private String receiptCode;
    private String materialName;
    private String unitName;
    private int quantity;
    private String department;
    private String recipient;
    private String note;
}