package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borrow_ticket")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String borrowCode;

    @Column(unique = true)
    private String returnCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "borrow_ticket_item", joinColumns = @JoinColumn(name = "ticket_id"))
    @Builder.Default
    private List<BorrowTicketItem> items = new ArrayList<>();
    
    private String borrowerName;
    private String department;
    private String email;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime borrowTime; // Thời gian admin duyệt mượn thực tế
    private LocalDateTime returnTime; // Thời gian trả đồ thực tế
    private java.time.LocalDate expectedReturnDate; // Thời gian dự kiến trả (do user chọn)

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}