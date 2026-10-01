package com.github.chaunguyentruongan.warehouse_cdnsg.modules.water_import;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaterImportService {

    private final WaterImportRepository waterImportRepository;
    private final WaterZoneRepository waterZoneRepository;

    private WaterImport getEntityById(Long id) {
        return waterImportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu nhập nước id: " + id));
    }

    private WaterImportResponse mapToResponse(WaterImport entity) {
        List<WaterImportResponse.DetailResponse> detailResponses = new ArrayList<>();
        if (entity.getDetails() != null) {
            detailResponses = entity.getDetails().stream()
                    .map(d -> WaterImportResponse.DetailResponse.builder()
                            .id(d.getId())
                            .zoneId(d.getZone().getId())
                            .zoneName(d.getZone().getName())
                            .quantity(d.getQuantity())
                            .build())
                    .collect(Collectors.toList());
        }

        return WaterImportResponse.builder()
                .id(entity.getId())
                .importDate(entity.getImportDate())
                .quantity(entity.getQuantity())
                .productionDate(entity.getProductionDate())
                .shellQuantity(entity.getShellQuantity())
                .details(detailResponses)
                .build();
    }

    @Transactional
    public WaterImportResponse create(WaterImportRequest request) {
        WaterImport waterImport = WaterImport.builder()
                .importDate(request.getImportDate() != null ? request.getImportDate() : LocalDate.now())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .productionDate(request.getProductionDate())
                .shellQuantity(request.getShellQuantity() != null ? request.getShellQuantity() : 0)
                .details(new ArrayList<>())
                .build();

        if (request.getDetails() != null) {
            int detailSum = 0;
            for (WaterImportRequest.Detail dReq : request.getDetails()) {
                WaterZone zone = waterZoneRepository.findById(dReq.getZoneId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Khu vực id: " + dReq.getZoneId()));

                WaterImportDetail detail = WaterImportDetail.builder()
                        .waterImport(waterImport)
                        .zone(zone)
                        .quantity(dReq.getQuantity() != null ? dReq.getQuantity() : 0)
                        .build();
                waterImport.getDetails().add(detail);
                detailSum += detail.getQuantity();
            }
            if (waterImport.getQuantity() == 0) {
                waterImport.setQuantity(detailSum);
            }
        }

        return mapToResponse(waterImportRepository.save(waterImport));
    }

    @Transactional
    public WaterImportResponse update(Long id, WaterImportRequest request) {
        WaterImport waterImport = getEntityById(id);

        waterImport.setImportDate(request.getImportDate() != null ? request.getImportDate() : LocalDate.now());
        waterImport.setQuantity(request.getQuantity() != null ? request.getQuantity() : 0);
        waterImport.setProductionDate(request.getProductionDate());
        waterImport.setShellQuantity(request.getShellQuantity() != null ? request.getShellQuantity() : 0);

        waterImport.getDetails().clear();
        if (request.getDetails() != null) {
            int detailSum = 0;
            for (WaterImportRequest.Detail dReq : request.getDetails()) {
                WaterZone zone = waterZoneRepository.findById(dReq.getZoneId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Khu vực id: " + dReq.getZoneId()));

                WaterImportDetail detail = WaterImportDetail.builder()
                        .waterImport(waterImport)
                        .zone(zone)
                        .quantity(dReq.getQuantity() != null ? dReq.getQuantity() : 0)
                        .build();
                waterImport.getDetails().add(detail);
                detailSum += detail.getQuantity();
            }
            if (waterImport.getQuantity() == 0) {
                waterImport.setQuantity(detailSum);
            }
        }

        return mapToResponse(waterImportRepository.save(waterImport));
    }

    @Transactional(readOnly = true)
    public WaterImportResponse findById(Long id) {
        return mapToResponse(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<WaterImportResponse> getAll(LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        Page<WaterImport> page = waterImportRepository.searchWithFilters(fromDate, toDate, pageable);
        return page.map(this::mapToResponse);
    }

    @Transactional
    public void delete(Long id) {
        WaterImport waterImport = getEntityById(id);
        waterImportRepository.delete(waterImport);
    }

    @Transactional(readOnly = true)
    public WaterImportReportResponse getReport(LocalDate fromDate, LocalDate toDate) {
        List<WaterImport> list = waterImportRepository.findAllBetweenDates(fromDate, toDate);

        long totalImport = 0;
        long totalShell = 0;

        Map<LocalDate, WaterImportReportResponse.DailySummary> dailyMap = new TreeMap<>();
        Map<Long, WaterImportReportResponse.ZoneSummary> zoneMap = new HashMap<>();

        for (WaterImport w : list) {
            totalImport += w.getQuantity();
            totalShell += w.getShellQuantity();

            // Daily Summary
            LocalDate date = w.getImportDate();
            dailyMap.compute(date, (k, v) -> {
                if (v == null) {
                    return WaterImportReportResponse.DailySummary.builder()
                            .date(date)
                            .importQuantity((long) w.getQuantity())
                            .shellQuantity((long) w.getShellQuantity())
                            .build();
                } else {
                    v.setImportQuantity(v.getImportQuantity() + w.getQuantity());
                    v.setShellQuantity(v.getShellQuantity() + w.getShellQuantity());
                    return v;
                }
            });

            // Zone Summary
            if (w.getDetails() != null) {
                for (WaterImportDetail d : w.getDetails()) {
                    WaterZone zone = d.getZone();
                    zoneMap.compute(zone.getId(), (k, v) -> {
                        if (v == null) {
                            return WaterImportReportResponse.ZoneSummary.builder()
                                    .zoneId(zone.getId())
                                    .zoneName(zone.getName())
                                    .quantity((long) d.getQuantity())
                                    .build();
                        } else {
                            v.setQuantity(v.getQuantity() + d.getQuantity());
                            return v;
                        }
                    });
                }
            }
        }

        return WaterImportReportResponse.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .totalImportQuantity(totalImport)
                .totalShellQuantity(totalShell)
                .dailySummaries(new ArrayList<>(dailyMap.values()))
                .zoneSummaries(new ArrayList<>(zoneMap.values()))
                .build();
    }
}
