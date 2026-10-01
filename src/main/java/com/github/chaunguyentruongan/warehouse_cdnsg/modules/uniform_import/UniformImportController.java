package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_import;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/uniform-imports")
@RequiredArgsConstructor
@Tag(name = "Quản lý Nhập kho Đồng phục")
public class UniformImportController {
    private final UniformImportService importService;

    @PostMapping
    public ResponseEntity<UniformImport> create(@RequestBody UniformImportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<UniformImport>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String name) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(importService.getAll(fromDate, toDate, note, name, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UniformImport> update(@PathVariable Long id, @RequestBody UniformImportRequest request) {
        return ResponseEntity.ok(importService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        importService.delete(id);
        return ResponseEntity.noContent().build();
    }
}