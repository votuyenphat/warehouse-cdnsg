package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_import;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_core.Uniform;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "uniform_import_detail")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniformImportDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "uniform_import_id")
    @JsonBackReference
    private UniformImport uniformImport;

    @ManyToOne
    @JoinColumn(name = "uniform_id")
    private Uniform uniform;

    private Long quantity;
}