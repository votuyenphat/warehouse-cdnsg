package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_loan;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProjectorLoanRequest {
    private List<Long> projectorIds; // THAY ĐỔI: Nhận nhiều máy chiếu cùng lúc
    private String borrower;
    private LocalDateTime borrowDate;
    private LocalDateTime returnDate;
    private String note;
}