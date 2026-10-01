package com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportReportFlatDTO {
    private LocalDate importDate;
    private String receiptCode;
    private String materialName;
    private String unitName;
    private int quantity;
    private String note;
}