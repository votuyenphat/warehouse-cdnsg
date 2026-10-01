package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import lombok.Data;

@Data
public class ItemReturnRequest {
    private Long itemId;
    private int returnedQuantity;
    private int brokenQuantity;
    private String conditionNote; // Ghi chú tình trạng (vd: Xước móp, bình thường...)
}
