package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

// File: ConfirmReturnDTO.java
@Data
public class ConfirmReturnDTO {
    private String returnCode;
    private Long ticketId; // Dùng cho trả thủ công nếu không có mã QR
    private java.util.List<ItemReturnRequest> items; // Ghi chú từng món
    private String generalNote; // Ghi chú chung
}