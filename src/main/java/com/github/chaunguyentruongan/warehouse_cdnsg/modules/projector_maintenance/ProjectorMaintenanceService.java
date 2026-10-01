package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_maintenance;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.Projector;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.ProjectorRepository;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.ProjectorStatus;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectorMaintenanceService {

    private final ProjectorMaintenanceRepository projectorMaintenanceRepository;
    private final MaintenanceTicketRepository ticketRepository;
    private final ProjectorRepository projectorRepository;

    @Transactional
    public MaintenanceTicket createTicket(MaintenanceTicketRequest request) {
        // 1. Khởi tạo phiếu bảo trì chung
        MaintenanceTicket ticket = MaintenanceTicket.builder()
                .ticketCode(
                        request.getTicketCode() != null && !request.getTicketCode().isEmpty() ? request.getTicketCode()
                                : "BT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .technician(request.getTechnician())
                .generalNote(request.getGeneralNote())
                .build();

        List<ProjectorMaintenanceDetail> details = new ArrayList<>();

        // 2. SỬA LỖI TẠI ĐÂY: Duyệt qua mảng projectorIds thay vì details
        if (request.getProjectorIds() != null && !request.getProjectorIds().isEmpty()) {
            for (Long projectorId : request.getProjectorIds()) {
                Projector projector = projectorRepository.findByIdWithLock(projectorId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Không tìm thấy máy chiếu ID: " + projectorId));

                // Kiểm tra trạng thái máy
                if (projector.getStatus() == ProjectorStatus.BORROWED
                        || projector.getStatus() == ProjectorStatus.IN_USE) {
                    throw new IllegalArgumentException("Máy chiếu " + projector.getName() + " đang bận!");
                }

                // Đổi trạng thái máy thành Đang bảo trì
                projector.setStatus(ProjectorStatus.UNDER_MAINTENANCE);
                projectorRepository.save(projector);

                // Tạo chi tiết bảo trì
                ProjectorMaintenanceDetail detail = ProjectorMaintenanceDetail.builder()
                        .ticket(ticket)
                        .projector(projector)
                        .build();
                details.add(detail);
            }
        }

        // 3. Lưu vào database
        ticket.setDetails(details);
        return ticketRepository.save(ticket);
    }

    // THÊM MỚI: Cập nhật phiếu bảo trì đang diễn ra
    @Transactional
    public MaintenanceTicket updateTicket(Long ticketId, MaintenanceTicketRequest request) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu bảo trì!"));

        if (ticket.getCompletionDate() != null) {
            throw new IllegalArgumentException("Không thể sửa phiếu bảo trì đã hoàn tất!");
        }

        // Cập nhật thông tin chung
        ticket.setTicketCode(request.getTicketCode());
        if (request.getStartDate() != null)
            ticket.setStartDate(request.getStartDate());
        ticket.setTechnician(request.getTechnician());
        ticket.setGeneralNote(request.getGeneralNote());

        List<ProjectorMaintenanceDetail> currentDetails = ticket.getDetails();
        List<MaintenanceDetailRequest> requestedDetails = request.getDetails() != null ? request.getDetails() : new ArrayList<>();

        // 1. Gỡ bỏ những máy chiếu không còn trong danh sách request (bị uncheck)
        List<ProjectorMaintenanceDetail> toRemove = new ArrayList<>();
        for (ProjectorMaintenanceDetail existingDetail : currentDetails) {
            boolean isKept = requestedDetails.stream()
                    .anyMatch(req -> req.getProjectorId().equals(existingDetail.getProjector().getId()));
            if (!isKept) {
                Projector projector = existingDetail.getProjector();
                projector.setStatus(ProjectorStatus.AVAILABLE); // Trả lại trạng thái rảnh
                projectorRepository.save(projector);
                toRemove.add(existingDetail);
            }
        }
        currentDetails.removeAll(toRemove);

        // 2. Cập nhật máy cũ hoặc Thêm máy mới vào phiếu
        for (MaintenanceDetailRequest reqDetail : requestedDetails) {
            ProjectorMaintenanceDetail existingDetail = currentDetails.stream()
                    .filter(d -> d.getProjector().getId().equals(reqDetail.getProjectorId()))
                    .findFirst()
                    .orElse(null);

            if (existingDetail != null) {
                // Đã có trong phiếu -> Cập nhật mô tả, chi phí
                existingDetail.setDescription(reqDetail.getDescription());
                existingDetail.setCost(reqDetail.getCost());
            } else {
                // Máy mới được thêm vào phiếu
                Projector projector = projectorRepository.findByIdWithLock(reqDetail.getProjectorId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy máy chiếu ID: " + reqDetail.getProjectorId()));

                if (projector.getStatus() == ProjectorStatus.BORROWED
                        || projector.getStatus() == ProjectorStatus.IN_USE) {
                    throw new IllegalArgumentException("Máy chiếu " + projector.getName() + " đang bận!");
                }

                projector.setStatus(ProjectorStatus.UNDER_MAINTENANCE);
                projectorRepository.save(projector);

                ProjectorMaintenanceDetail newDetail = ProjectorMaintenanceDetail.builder()
                        .ticket(ticket)
                        .projector(projector)
                        .description(reqDetail.getDescription())
                        .cost(reqDetail.getCost())
                        .build();
                currentDetails.add(newDetail);
            }
        }

        return ticketRepository.save(ticket);
    }

    // Thêm các hàm này vào ProjectorMaintenanceService.java

    // 1. Lấy danh sách phiếu bảo trì có phân trang và lọc
    public Page<MaintenanceTicket> getAllTickets(String keyword, String status, Pageable pageable) {
        return ticketRepository.searchWithFilter(keyword, status, pageable);
    }

    // 2. Lấy lịch sử bảo trì của một máy chiếu cụ thể
    public List<ProjectorMaintenanceDetail> getHistoryByProjectorId(Long projectorId) {
        return projectorMaintenanceRepository.findHistoryByProjectorId(projectorId);
    }

    // 3. Logic Hoàn tất phiếu bảo trì (Chốt phiếu)
    @Transactional
    public MaintenanceTicket completeTicket(Long ticketId, CompleteMaintenanceRequest request) {
        MaintenanceTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu bảo trì ID: " + ticketId));

        if (ticket.getCompletionDate() != null) {
            throw new IllegalArgumentException("Phiếu bảo trì này đã hoàn tất trước đó!");
        }

        // Cập nhật ngày hoàn thành
        ticket.setCompletionDate(request.getCompletionDate() != null ? request.getCompletionDate() : LocalDate.now());

        // Cập nhật trạng thái máy chiếu dựa trên danh sách results từ Frontend gửi về
        if (request.getResults() != null) {
            for (CompleteMaintenanceRequest.MaintenanceResult res : request.getResults()) {
                // Tìm máy chiếu dựa trên projectorId gửi từ DTO
                Projector projector = projectorRepository.findById(res.getProjectorId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Không tìm thấy máy chiếu ID: " + res.getProjectorId()));

                // Cập nhật trạng thái mới (AVAILABLE hoặc BROKEN)
                projector.setStatus(res.getStatus());
                projectorRepository.save(projector);
            }
        }

        return ticketRepository.save(ticket);
    }

    @Transactional
    public void deleteByProjectorId(Long id) {
        projectorMaintenanceRepository.deleteByProjectorId(id);
    }

    @Transactional
    public void deleteTicket(Long id) {
        projectorMaintenanceRepository.deleteByTicketId(id);
        ticketRepository.deleteById(id);
    }
}