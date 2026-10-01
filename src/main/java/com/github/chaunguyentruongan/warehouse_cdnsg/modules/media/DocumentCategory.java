package com.github.chaunguyentruongan.warehouse_cdnsg.modules.media;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Tên loại (VD: Hợp đồng, Hóa đơn...)
}