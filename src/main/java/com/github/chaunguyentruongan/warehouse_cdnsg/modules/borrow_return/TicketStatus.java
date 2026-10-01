package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

public enum TicketStatus {
    PENDING,    // Đang chờ duyệt
    BORROWED,   // Đang cho mượn
    RETURNED,   // Đã trả
    REJECTED,   // Từ chối
    OVERDUE,    // Quá hạn
    INCOMPLETE, // Trả thiếu / hỏng
    
    // Giữ lại để tương thích ngược với dữ liệu cũ trong Database
    NEW,
    COMPLETED
}