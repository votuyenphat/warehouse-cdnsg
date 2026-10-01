package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core;

public enum ProjectorStatus {
    AVAILABLE, // Sẵn sàng sử dụng
    BORROWED, // Đang cho mượn
    IN_USE, // Đang sử dụng (tại chỗ)
    UNDER_MAINTENANCE, // Đang bảo trì
    BROKEN // Đang hỏng/Chờ xử lý
}