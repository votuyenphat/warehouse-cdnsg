package com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO nhận từ Frontend khi muốn lưu nhiều thay đổi ô cùng lúc (batch).
 * Cho phép gộp nhiều ô thay đổi trong 1 request thay vì gửi từng request một.
 */
@Data
public class SaveLogsRequest {

    /** ID của file đang được chỉnh sửa */
    private Long fileId;

    /** Danh sách các ô đã thay đổi */
    private List<CellChangeDTO> changes;

    @Data
    public static class CellChangeDTO {
        /** Tên sheet trong file Excel */
        private String sheetName;

        /** Địa chỉ ô: A1, B5, Z100... */
        private String cellAddress;

        /** Giá trị cũ (trước khi sửa) */
        private String oldValue;

        /** Giá trị mới (sau khi sửa) */
        private String newValue;
    }
}
