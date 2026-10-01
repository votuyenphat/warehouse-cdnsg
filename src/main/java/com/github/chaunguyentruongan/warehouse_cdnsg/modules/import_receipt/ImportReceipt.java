package com.github.chaunguyentruongan.warehouse_cdnsg.modules.import_receipt;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "import_receipt")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate importDate;
    private String note;

    @OneToMany(mappedBy = "importReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ImportItem> importItems;

    @Column(unique = true, nullable = false)
    private String receiptCode; // Mã phiếu (VD: PN-20260329-001)

    private String createdBy; // Tên người lập phiếu (Tạm dùng String, sau này có Login thì map với User)

    @Enumerated(EnumType.STRING)
    private ReceiptStatus status = ReceiptStatus.COMPLETED;
}
