package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow-items")
@RequiredArgsConstructor
@Tag(name = "Borrow Item Management", description = "Quản lý danh mục vật phẩm cho mượn (độc lập với kho material)")
public class BorrowItemController {

    private final BorrowItemService itemService;

    @Operation(summary = "Thêm vật phẩm mới")
    @PostMapping
    public ResponseEntity<BorrowItem> createItem(@RequestBody BorrowItemRequest request) {
        return ResponseEntity.ok(itemService.createItem(request));
    }

    @Operation(summary = "Cập nhật vật phẩm")
    @PutMapping("/{id}")
    public ResponseEntity<BorrowItem> updateItem(@PathVariable Long id, @RequestBody BorrowItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    @Operation(summary = "Lấy danh sách vật phẩm")
    @GetMapping
    public ResponseEntity<Page<BorrowItem>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "false") boolean availableOnly) {
        return ResponseEntity.ok(itemService.findAll(keyword, category, availableOnly, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @Operation(summary = "Lấy chi tiết vật phẩm")
    @GetMapping("/{id}")
    public ResponseEntity<BorrowItem> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @Operation(summary = "Xóa vật phẩm")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok().build();
    }
}
