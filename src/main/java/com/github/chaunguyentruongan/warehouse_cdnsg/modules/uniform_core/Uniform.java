package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_import.UniformImportDetail;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_receipt.UniformReceiptDetail;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Table(name = "uniform", uniqueConstraints = {
        // Đảm bảo không có 2 dòng nào trùng cả Type và Size
        @UniqueConstraint(columnNames = { "type", "size" })
})
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Uniform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String size;

    private Long stock;

    // TỰ ĐỘNG XÓA: Khi xóa Uniform, xóa luôn các chi tiết trong Phiếu Nhập chứa nó
    @JsonIgnore
    @OneToMany(mappedBy = "uniform", cascade = CascadeType.REMOVE)
    private List<UniformImportDetail> importDetails;

    // TỰ ĐỘNG XÓA: Khi xóa Uniform, xóa luôn các chi tiết trong Phiếu Xuất chứa nó
    @JsonIgnore
    @OneToMany(mappedBy = "uniform", cascade = CascadeType.REMOVE)
    private List<UniformReceiptDetail> receiptDetails;
}