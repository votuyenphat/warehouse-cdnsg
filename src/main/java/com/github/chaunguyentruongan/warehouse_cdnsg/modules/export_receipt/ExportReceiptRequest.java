package com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt;

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
public class ExportReceiptRequest {
    private LocalDate exportDate;
    private String note;
    private String invoiceCode;
    private String department;
    private String recipient;
    private List<ExportItemRequest> exportItemRequests;
}