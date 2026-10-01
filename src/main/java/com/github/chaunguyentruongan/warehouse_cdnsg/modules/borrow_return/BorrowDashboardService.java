package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BorrowDashboardService {

    private final BorrowItemRepository itemRepository;
    private final BorrowTicketRepository ticketRepository;

    public Map<String, Object> getKpis() {
        ticketRepository.updateOverdueTickets(java.time.LocalDate.now());
        
        Map<String, Object> kpis = new HashMap<>();
        
        long totalItems = itemRepository.sumTotalQuantity();
        long availableItems = itemRepository.sumAvailableQuantity();
        long borrowedItems = totalItems - availableItems;
        
        kpis.put("totalItems", totalItems);
        kpis.put("availableItems", availableItems);
        kpis.put("borrowedItems", borrowedItems);
        kpis.put("pendingTickets", ticketRepository.countByStatus(TicketStatus.PENDING));
        kpis.put("overdueTickets", ticketRepository.countByStatus(TicketStatus.OVERDUE));
        
        return kpis;
    }

    public Map<String, Object> getQuickLists() {
        ticketRepository.updateOverdueTickets(java.time.LocalDate.now());
        
        Map<String, Object> lists = new HashMap<>();
        
        // Top 5 đơn chờ xử lý
        lists.put("topPendingTickets", ticketRepository.searchWithFilter(null, TicketStatus.PENDING, PageRequest.of(0, 5, Sort.by("createdAt").descending())).getContent());
        
        // Các đơn quá hạn
        lists.put("overdueTickets", ticketRepository.searchWithFilter(null, TicketStatus.OVERDUE, PageRequest.of(0, 10, Sort.by("borrowTime").ascending())).getContent());
        
        // Các vật phẩm sắp hết (availableQuantity < 3)
        lists.put("lowStockItems", itemRepository.findLowStockItems(3));
        
        return lists;
    }
}
