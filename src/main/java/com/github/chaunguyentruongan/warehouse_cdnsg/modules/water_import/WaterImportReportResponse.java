package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterImportReportResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private Long totalImportQuantity;
    private Long totalShellQuantity;
    private List<DailySummary> dailySummaries;
    private List<ZoneSummary> zoneSummaries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySummary {
        private LocalDate date;
        private Long importQuantity;
        private Long shellQuantity;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneSummary {
        private Long zoneId;
        private String zoneName;
        private Long quantity;
    }
}
