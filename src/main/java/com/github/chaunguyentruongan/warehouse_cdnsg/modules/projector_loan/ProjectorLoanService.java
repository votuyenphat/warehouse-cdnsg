package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_loan;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.Projector;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.ProjectorRepository;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.ProjectorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectorLoanService {
    private final ProjectorLoanRepository loanRepository;
    private final ProjectorRepository projectorRepository;

    @Transactional
    public List<ProjectorLoan> borrowProjector(ProjectorLoanRequest request) {
        if (request.getProjectorIds() == null || request.getProjectorIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 máy chiếu!");
        }

        List<ProjectorLoan> createdLoans = new ArrayList<>();

        for (Long pId : request.getProjectorIds()) {
            Projector projector = projectorRepository.findByIdWithLock(pId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy máy chiếu ID: " + pId));

            if (projector.getStatus() != ProjectorStatus.AVAILABLE) {
                throw new IllegalArgumentException("Máy chiếu " + projector.getName() + " không rảnh!");
            }

            projector.setStatus(ProjectorStatus.BORROWED);
            projectorRepository.save(projector);

            ProjectorLoan loan = ProjectorLoan.builder()
                    .projector(projector)
                    .borrower(request.getBorrower())
                    .borrowDate(request.getBorrowDate() != null ? request.getBorrowDate() : LocalDateTime.now())
                    .status(LoanStatus.BORROWING)
                    .note(request.getNote())
                    .build();

            createdLoans.add(loanRepository.save(loan));
        }
        return createdLoans;
    }

    // CẢI TIẾN: Cho phép truyền trạng thái của máy chiếu sau khi trả
    @Transactional
    public ProjectorLoan returnProjector(Long loanId, String returnNote, ProjectorStatus nextStatus) {
        ProjectorLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn!"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalArgumentException("Phiếu này đã được hoàn tất trả máy từ trước!");
        }

        loan.setStatus(LoanStatus.RETURNED);
        loan.setReturnDate(LocalDateTime.now());
        loan.setNote(loan.getNote() + " | Ghi chú khi trả: " + (returnNote != null ? returnNote : ""));

        Projector projector = loan.getProjector();

        // Nếu người dùng không truyền nextStatus, mặc định máy chiếu trả về là dùng
        // được (AVAILABLE)
        // Nếu trả về mà máy bị hỏng, họ có thể truyền BROKEN hoặc UNDER_MAINTENANCE
        ProjectorStatus targetStatus = (nextStatus != null) ? nextStatus : ProjectorStatus.AVAILABLE;
        projector.setStatus(targetStatus);

        projectorRepository.save(projector);

        return loanRepository.save(loan);
    }

    // CẢI TIẾN: Thêm tính năng sửa phiếu mượn (đang mượn)
    @Transactional
    public ProjectorLoan updateLoan(Long loanId, ProjectorLoanRequest request) {
        ProjectorLoan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu mượn!"));

        // // Chỉ cho phép sửa khi phiếu đang ở trạng thái mượn
        // if (loan.getStatus() == LoanStatus.RETURNED) {
        // throw new IllegalArgumentException("Không thể sửa phiếu đã hoàn tất trả
        // máy!");
        // }

        loan.setBorrower(request.getBorrower());
        if (request.getBorrowDate() != null) {
            loan.setBorrowDate(request.getBorrowDate());
        }
        if (request.getReturnDate() != null) {
            loan.setReturnDate(request.getReturnDate());
        }
        loan.setNote(request.getNote());

        return loanRepository.save(loan);
    }

    public Page<ProjectorLoan> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    public Page<ProjectorLoan> getAllWithFilter(String keyword, LoanStatus status, Pageable pageable) {
        return loanRepository.searchWithFilter(keyword, status, pageable);
    }

    // Hàm lấy lịch sử mượn trả của riêng 1 máy chiếu
    public List<ProjectorLoan> getHistoryByProjectorId(Long projectorId) {
        return loanRepository.findByProjectorIdOrderByBorrowDateDesc(projectorId);
    }

    @Transactional
    public void deleteByProjectorId(Long projectorId) {
        List<ProjectorLoan> loans = loanRepository.findByProjectorIdOrderByBorrowDateDesc(projectorId);
        loanRepository.deleteAll(loans);
    }

    public List<ProjectorUsageDTO> getProjectorUsageReport() {
        List<Object[]> results = loanRepository.getUsageStatsNative();
        List<ProjectorUsageDTO> dtos = new ArrayList<>();

        for (Object[] row : results) {
            dtos.add(new ProjectorUsageDTO(
                    ((Number) row[0]).longValue(), // id
                    (String) row[1], // name
                    (String) row[2], // serialNumber
                    row[3] != null ? ((Number) row[3]).longValue() : 0L // totalSeconds
            ));
        }
        return dtos;
    }
}