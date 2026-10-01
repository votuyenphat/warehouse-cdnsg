package com.github.chaunguyentruongan.warehouse_cdnsg.modules.inventory.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin metadata của một file inventory.
 * Không expose publicId ra ngoài để bảo mật.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryFileResponse {
    private Long id;
    private String fileName;
    private String departmentName;
    private String cloudinaryUrl;
    private Long fileSize;
    private String fileType;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
