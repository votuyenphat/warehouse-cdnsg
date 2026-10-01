package com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.Material;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "export_item_detail")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "export_receipt_id")
    @JsonBackReference
    private ExportReceipt exportReceipt;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    private int quantity;
}