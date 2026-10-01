package com.github.chaunguyentruongan.warehouse_cdnsg.modules.media;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/document-categories")
@RequiredArgsConstructor
public class DocumentCategoryController {

    private final DocumentCategoryRepository repository;

    @GetMapping
    public ResponseEntity<List<DocumentCategory>> getAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<DocumentCategory> create(@RequestBody DocumentCategory category) {
        return ResponseEntity.ok(repository.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}