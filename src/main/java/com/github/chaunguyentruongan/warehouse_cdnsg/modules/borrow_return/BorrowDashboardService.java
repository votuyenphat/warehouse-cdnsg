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
        
        long totalItems = 0;
        long availableItems = 0;
        java.util.List<Object[]> qtyList = itemRepository.sumQuantities();
        if (qtyList != null && !qtyList.isEmpty() && qtyList.get(0) != null) {
            Object[] qRow = qtyList.get(0);
            totalItems = qRow[0] != null ? ((Number) qRow[0]).longValue() : 0L;
            availableItems = qRow[1] != null ? ((Number) qRow[1]).longValue() : 0L;
        }
        long borrowedItems = Math.max(0, totalItems - availableItems);

        long pendingTickets = 0;
        long overdueTickets = 0;
        java.util.List<Object[]> ticketCounts = ticketRepository.countPendingAndOverdue();
        if (ticketCounts != null && !ticketCounts.isEmpty() && ticketCounts.get(0) != null) {
            Object[] tRow = ticketCounts.get(0);
            pendingTickets = tRow[0] != null ? ((Number) tRow[0]).longValue() : 0L;
            overdueTickets = tRow[1] != null ? ((Number) tRow[1]).longValue() : 0L;
        }

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalItems", totalItems);
        kpis.put("availableItems", availableItems);
        kpis.put("borrowedItems", borrowedItems);
        kpis.put("pendingTickets", pendingTickets);
        kpis.put("overdueTickets", overdueTickets);

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
