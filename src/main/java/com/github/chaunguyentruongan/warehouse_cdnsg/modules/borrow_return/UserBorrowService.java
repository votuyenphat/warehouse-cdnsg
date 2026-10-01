package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserBorrowService {

    private final BorrowTicketRepository ticketRepository;

    public Map<String, Object> getUserDashboard(String email) {
        ticketRepository.updateOverdueTickets(java.time.LocalDate.now());
        
        Map<String, Object> dashboard = new HashMap<>();
        
        long totalBorrows = ticketRepository.countByEmail(email);
        long holdingItems = ticketRepository.countHoldingItemsByEmail(email);
        long overdueCount = ticketRepository.countByEmailAndStatus(email, TicketStatus.OVERDUE);

        dashboard.put("totalBorrows", totalBorrows);
        dashboard.put("holdingItems", holdingItems);
        dashboard.put("overdueCount", overdueCount);
        
        return dashboard;
    }

    public Page<BorrowTicket> getUserHistory(String email, TicketStatus status, Pageable pageable) {
        ticketRepository.updateOverdueTickets(java.time.LocalDate.now());
        return ticketRepository.findByEmailAndOptionalStatus(email, status, pageable);
    }
}
