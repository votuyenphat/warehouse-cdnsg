package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterImportResponse {
    private Long id;
    private LocalDate importDate;
    private Integer quantity;
    private LocalDate productionDate;
    private Integer shellQuantity;
    private List<DetailResponse> details;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailResponse {
        private Long id;
        private Long zoneId;
        private String zoneName;
        private Integer quantity;
    }
}
