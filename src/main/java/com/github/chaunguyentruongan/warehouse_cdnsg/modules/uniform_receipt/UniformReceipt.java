package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_receipt;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "uniform_receipt")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UniformReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Đã đổi từ varchar -> LocalDate để chuẩn hóa
    private LocalDate date;

    // Đã đổi từ bigint -> String để lưu tên
    @Column(name = "cus_name")
    private String cusName;

    @Column(name = "total_quantity")
    private Long totalQuantity;

    @OneToMany(mappedBy = "uniformReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<UniformReceiptDetail> details = new ArrayList<>();
}