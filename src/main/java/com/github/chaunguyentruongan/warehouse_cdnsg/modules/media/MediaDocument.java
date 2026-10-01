package com.github.chaunguyentruongan.warehouse_cdnsg.modules.media;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "media_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Tên hiển thị

    @Column(nullable = false)
    private String fileUrl; // Link trả về từ Cloudinary

    @Column(nullable = false)
    private String publicId; // Dùng để xóa file trên Cloudinary

    private String fileType; // VD: IMAGE, PDF, DOC...

    private String category; // Phân loại (VD: CONTRACT, INVOICE, AVATAR)

    private Long fileSize; // Kích thước (bytes)

    private LocalDateTime uploadAt; // Ngày tải lên

    private String description; // Ghi chú thêm
}