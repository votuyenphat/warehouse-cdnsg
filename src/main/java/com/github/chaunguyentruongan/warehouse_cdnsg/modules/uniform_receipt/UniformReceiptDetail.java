package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_receipt;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_core.Uniform;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "uniform_receipt_detail")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniformReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uniform_receipt_id") // Sửa lại tên cột khóa ngoại cho chuẩn convention
    @JsonBackReference
    private UniformReceipt uniformReceipt;

    @ManyToOne
    @JoinColumn(name = "uniform_id")
    private Uniform uniform;

    private Long quantity;
}