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
public class ImportReceiptRequest {
    private LocalDate importDate;
    private String invoiceCode;
    private String note;
    private List<ImportItemRequest> importItemRequests;
}
