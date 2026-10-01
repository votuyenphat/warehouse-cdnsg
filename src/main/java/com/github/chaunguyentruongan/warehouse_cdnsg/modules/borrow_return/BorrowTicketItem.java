package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowTicketItem {
    private Long itemId;
    private String itemName;
    private Integer quantity;

    private Integer returnedQuantity;
    private Integer brokenQuantity;
    private String conditionNote;
}