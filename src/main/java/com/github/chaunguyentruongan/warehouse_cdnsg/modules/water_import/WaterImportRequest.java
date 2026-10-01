package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class WaterImportRequest {
    private LocalDate importDate;
    private Integer quantity;
    private LocalDate productionDate;
    private Integer shellQuantity;
    private List<Detail> details;

    @Getter
    @Setter
    public static class Detail {
        private Long zoneId;
        private Integer quantity;
    }
}
