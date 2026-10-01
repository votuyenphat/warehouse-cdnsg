package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BorrowItemService {

    private final BorrowItemRepository itemRepository;

    @Transactional
    public BorrowItem createItem(BorrowItemRequest request) {
        BorrowItem item = BorrowItem.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .unit(request.getUnit())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .brokenLostQuantity(0)
                .pendingQuantity(0)
                .borrowedQuantity(0)
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .deleted(false)
                .build();
        return itemRepository.save(item);
    }

    @Transactional
    public BorrowItem updateItem(Long id, BorrowItemRequest request) {
        BorrowItem item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm"));
        
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setDescription(request.getDescription());
        item.setUnit(request.getUnit());
        
        int diff = request.getTotalQuantity() - item.getTotalQuantity();
        item.setTotalQuantity(request.getTotalQuantity());
        item.setAvailableQuantity(item.getAvailableQuantity() + diff);

        return itemRepository.save(item);
    }

    public Page<BorrowItem> findAll(String keyword, String category, boolean availableOnly, Pageable pageable) {
        return itemRepository.searchWithFilter(keyword, category, availableOnly, pageable);
    }

    public BorrowItem findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm"));
    }

    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }

    @Transactional
    public void updateStockOnCreateTicket(Long itemId, int quantity) {
        BorrowItem item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm"));
        item.setPendingQuantity(item.getPendingQuantity() + quantity);
        itemRepository.save(item);
    }

    @Transactional
    public void updateStockOnApproveTicket(Long itemId, int quantity) {
        BorrowItem item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm"));
        if (item.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Số lượng khả dụng không đủ cho: " + item.getName());
        }
        item.setPendingQuantity(Math.max(0, item.getPendingQuantity() - quantity));
        item.setAvailableQuantity(item.getAvailableQuantity() - quantity);
        item.setBorrowedQuantity(item.getBorrowedQuantity() + quantity);
        itemRepository.save(item);
    }

    @Transactional
    public void updateStockOnRejectTicket(Long itemId, int quantity) {
        BorrowItem item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm"));
        item.setPendingQuantity(Math.max(0, item.getPendingQuantity() - quantity));
        itemRepository.save(item);
    }

    @Transactional
    public void updateStockOnReturnTicketDetailed(Long itemId, int returnedQuantity, int brokenQuantity) {
        BorrowItem item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm"));
        
        int totalProcessed = returnedQuantity + brokenQuantity;
        item.setBorrowedQuantity(Math.max(0, item.getBorrowedQuantity() - totalProcessed));
        item.setAvailableQuantity(item.getAvailableQuantity() + returnedQuantity);
        item.setBrokenLostQuantity(item.getBrokenLostQuantity() + brokenQuantity);
        
        itemRepository.save(item);
    }

    @Transactional
    public void updateStockOnResolveIncomplete(Long itemId, int missingQuantity, int brokenQuantity) {
        BorrowItem item = itemRepository.findByIdWithLock(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm"));
        if (missingQuantity > 0) {
            item.setBorrowedQuantity(Math.max(0, item.getBorrowedQuantity() - missingQuantity));
            item.setAvailableQuantity(item.getAvailableQuantity() + missingQuantity);
        }
        if (brokenQuantity > 0) {
            item.setBrokenLostQuantity(Math.max(0, item.getBrokenLostQuantity() - brokenQuantity));
            item.setAvailableQuantity(item.getAvailableQuantity() + brokenQuantity);
        }
        itemRepository.save(item);
    }
}
