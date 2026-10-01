package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import lombok.Data;

@Data
public class BorrowItemRequest {
    private String name;
    private String category;
    private String description;
    private String unit;
    private int totalQuantity;
    private String createdBy;
}
