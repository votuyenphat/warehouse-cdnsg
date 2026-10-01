package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "water_import")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "import_date", nullable = false)
    private LocalDate importDate;

    @Column(nullable = false)
    private Integer quantity; // Số lượng nhập

    @Column(name = "production_date")
    private LocalDate productionDate; // Ngày sản xuất

    @Column(name = "shell_quantity")
    private Integer shellQuantity; // Số lượng vỏ

    @OneToMany(mappedBy = "waterImport", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<WaterImportDetail> details = new ArrayList<>();
}
