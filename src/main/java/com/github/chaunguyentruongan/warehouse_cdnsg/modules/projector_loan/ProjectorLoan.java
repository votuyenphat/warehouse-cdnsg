package com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_loan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.projector_core.Projector;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "projector_loan")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProjectorLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projector_id", nullable = false)
    @JsonIgnoreProperties({ "loans", "maintenances" })
    private Projector projector;

    // Người mượn hoặc Nơi mượn (VD: "Phòng thực hành 1", "Giảng viên Nguyễn Văn A")
    @Column(nullable = false)
    private String borrower;

    @Column(nullable = false)
    private LocalDateTime borrowDate;

    // Ngày trả: Sẽ bị null khi trạng thái đang là BORROWING, được update khi người
    // dùng đem trả
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    private String note;
}