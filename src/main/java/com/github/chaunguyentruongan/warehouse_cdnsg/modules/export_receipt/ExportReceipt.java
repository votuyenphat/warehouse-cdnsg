package com.github.chaunguyentruongan.warehouse_cdnsg.modules.export_receipt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "export_receipt")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExportReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate exportDate;
    private String note;
    private String department;
    private String recipient;

    @OneToMany(mappedBy = "exportReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ExportItem> exportItems = new ArrayList<>();

    @Column(unique = true, nullable = false)
    private String receiptCode; // Mã phiếu (VD: PN-20260329-001)

    private String createdBy; // Tên người lập phiếu (Tạm dùng String, sau này có Login thì map với User)

    @Enumerated(EnumType.STRING)
    private ReceiptStatus status = ReceiptStatus.COMPLETED; // Trạng thái phiếu
}