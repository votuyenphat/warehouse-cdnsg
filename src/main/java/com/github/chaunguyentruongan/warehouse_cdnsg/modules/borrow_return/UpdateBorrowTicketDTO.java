package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import lombok.Data;

@Data
public class UpdateBorrowTicketDTO {
    private String borrowerName;
    private String department;
    private String note;
    private TicketStatus status;
}