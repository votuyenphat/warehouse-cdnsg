package com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.material.Material;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "import_item_detail")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "import_receipt_id")
    @JsonBackReference
    private ImportReceipt importReceipt;

    @ManyToOne
    @JoinColumn(name = "material_id")
    private Material material;

    private int quantity;
}
