package com.github.chaunguyentruongan.warehouse_cdnsg.modules.fire_extinguisher;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FireExtinguisherService {

    private final FireExtinguisherRepository repository;
    private final ExtinguisherHistoryRepository historyRepository;
    private final LocationRepository locationRepository;

    @Value("${app.extinguisher.warning-days:15}")
    private int warningDays;

    // ---------------- HELPER METHODS ---------------- //

    private FireExtinguisher getEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình chữa cháy id: " + id));
    }

    // HÀM MỚI: Tự động tính toán trạng thái dựa trên ngày hết hạn
    private MaintenanceStatus calculateStatus(LocalDate nextRechargeDate) {
        if (nextRechargeDate == null)
            return MaintenanceStatus.OK;

        LocalDate today = LocalDate.now();
        if (nextRechargeDate.isBefore(today) || nextRechargeDate.isEqual(today)) {
            return MaintenanceStatus.EXPIRED;
        } else if (nextRechargeDate.isBefore(today.plusDays(warningDays))) {
            return MaintenanceStatus.WARNING;
        }
        return MaintenanceStatus.OK;
    }

    private FireExtinguisherResponse mapToResponse(FireExtinguisher fe) {
        return FireExtinguisherResponse.builder()
                .id(fe.getId())
                .locationId(fe.getLocation().getId())
                .locationName(fe.getLocation().getName())
                .zoneId(fe.getLocation().getZone().getId())
                .zoneName(fe.getLocation().getZone().getName())
                .type(fe.getType())
                .weight(fe.getWeight())
                .quantity(fe.getQuantity())
                .unit(fe.getUnit())
                .note(fe.getNote())
                .lastRechargeDate(fe.getLastRechargeDate())
                .nextRechargeDate(fe.getNextRechargeDate())
                .status(fe.getStatus() != null ? fe.getStatus().name() : null)
                .build();
    }

    // ---------------- MAIN LOGIC ---------------- //

    public FireExtinguisherResponse findById(Long id) {
        return mapToResponse(getEntityById(id));
    }

    public List<ZoneExtinguisherStatsResponse> getAdvancedStats() {
        return repository.getAdvancedStats();
    }

    // ALTER TABLE fire_extinguisher ADD COLUMN type_normalized VARCHAR(255);
    public Page<FireExtinguisherResponse> getAll(String keyword, Long zoneId, String type, String weight,
            MaintenanceStatus status,
            Pageable pageable) {
        String safeKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim()
                : null;

        String safeType = (type != null && !type.trim().isEmpty())
                ? type.trim()
                : null;

        // Gọi thẳng hàm searchWithFilters với mọi tham số
        Page<FireExtinguisher> page = repository.searchWithFilters(safeKeyword, zoneId, safeType, weight, status,
                pageable);

        return page.map(this::mapToResponse);
    }

    public long countByStatus(MaintenanceStatus status) {
        return repository.countByStatus(status);
    }

    public List<ExtinguisherHistory> getHistory(Long id) {
        getEntityById(id);
        return historyRepository.findByExtinguisherIdOrderByRechargeDateDesc(id);
    }

    @Transactional
    public FireExtinguisherResponse create(FireExtinguisherRequest request) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy Vị trí id: " + request.getLocationId()));

        FireExtinguisher fe = FireExtinguisher.builder()
                .location(location)
                .type(request.getType())
                .weight(request.getWeight())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .note(request.getNote())
                .status(MaintenanceStatus.OK)
                .build();

        return mapToResponse(repository.save(fe));
    }

    @Transactional
    public FireExtinguisherResponse update(Long id, FireExtinguisherRequest request) {
        FireExtinguisher fe = getEntityById(id);

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Không tìm thấy Vị trí id: " + request.getLocationId()));

        fe.setLocation(location);
        fe.setType(request.getType());
        fe.setWeight(request.getWeight());
        fe.setQuantity(request.getQuantity());
        fe.setUnit(request.getUnit());
        fe.setNote(request.getNote());

        return mapToResponse(repository.save(fe));
    }

    @Transactional
    public void delete(Long id) {
        FireExtinguisher fe = getEntityById(id);
        List<ExtinguisherHistory> feHis = historyRepository.findByExtinguisherIdOrderByRechargeDateDesc(id);
        feHis.forEach(his -> {
            historyRepository.deleteById(his.getId());
        });
        repository.delete(fe);
    }

    // SỬA LỖI TẠI ĐÂY: Áp dụng trạng thái tức thời khi nạp bình
    @Transactional
    public FireExtinguisherResponse recharge(Long id, LocalDate rechargeDate, LocalDate nextRechargeDate, String note) {
        FireExtinguisher fe = getEntityById(id);

        LocalDate finalNextDate = (nextRechargeDate != null) ? nextRechargeDate : rechargeDate.plusMonths(6);
        String finalNote = (note != null && !note.trim().isEmpty()) ? note : "Nạp bình bảo dưỡng";

        fe.setLastRechargeDate(rechargeDate);
        fe.setNextRechargeDate(finalNextDate);

        // Thay thế fe.setStatus(MaintenanceStatus.OK) bằng hàm tự tính
        fe.setStatus(calculateStatus(finalNextDate));

        ExtinguisherHistory history = ExtinguisherHistory.builder()
                .extinguisher(fe)
                .rechargeDate(rechargeDate)
                .nextRechargeDate(finalNextDate)
                .note(finalNote)
                .build();
        historyRepository.save(history);

        return mapToResponse(repository.save(fe));
    }

    // SỬA LỖI TẠI ĐÂY: Áp dụng trạng thái tức thời khi nạp hàng loạt theo khu vực
    @Transactional
    public void rechargeByZone(Long zoneId, RechargeRequest request) {
        List<FireExtinguisher> extinguishers = repository.findAllByLocationZoneId(zoneId);
        if (extinguishers.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bình chữa cháy nào trong Khu vực id: " + zoneId);
        }

        LocalDate rechargeDate = request.getRechargeDate();
        LocalDate nextDate = (request.getNextRechargeDate() != null) ? request.getNextRechargeDate()
                : rechargeDate.plusMonths(6);
        String note = (request.getNote() != null && !request.getNote().trim().isEmpty()) ? request.getNote()
                : "Nạp hàng loạt theo khu vực";

        // Tính trước trạng thái để áp dụng cho cả lô
        MaintenanceStatus newStatus = calculateStatus(nextDate);

        for (FireExtinguisher fe : extinguishers) {
            fe.setLastRechargeDate(rechargeDate);
            fe.setNextRechargeDate(nextDate);

            // Thay thế bằng hàm tự tính
            fe.setStatus(newStatus);

            ExtinguisherHistory history = ExtinguisherHistory.builder()
                    .extinguisher(fe)
                    .rechargeDate(rechargeDate)
                    .nextRechargeDate(nextDate)
                    .note(note)
                    .build();
            historyRepository.save(history);
        }

        repository.saveAll(extinguishers);
    }

    // CẢI TIẾN: Hàm update lịch trình cũng sử dụng lại Helper Method để code gọn
    // gàng hơn
    @Transactional
    public void updateAllStatuses() {
        List<FireExtinguisher> list = repository.findAll();
        for (FireExtinguisher fe : list) {
            if (fe.getNextRechargeDate() != null) {
                fe.setStatus(calculateStatus(fe.getNextRechargeDate()));
            }
        }
        repository.saveAll(list);
    }

    @Scheduled(cron = "0 0 1 * * ?") // Vẫn giữ Cron job để tự quét mỗi 1h sáng
    @Transactional
    public void updateAllStatusesTask() {
        updateAllStatuses();
    }
}