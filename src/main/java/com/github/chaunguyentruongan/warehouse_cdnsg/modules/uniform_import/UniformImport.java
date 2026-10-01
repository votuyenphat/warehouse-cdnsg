package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_import;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.github.chaunguyentruongan.warehouse_cdnsg.enums.ReceiptStatus;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Table(name = "uniform_import")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UniformImport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String note;
    private ReceiptStatus status;

    @Column(name = "name_response")
    private String nameResponse;

    @OneToMany(mappedBy = "uniformImport", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<UniformImportDetail> details = new ArrayList<>();
}