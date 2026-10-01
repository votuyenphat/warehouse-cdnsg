package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TopMaterialDTO {
    private Material material;
    private Long totalExportedQuantity;
}