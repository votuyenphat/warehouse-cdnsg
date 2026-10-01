package com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportItemRequest {
    private Long materialId;
    private Integer quantity;
}