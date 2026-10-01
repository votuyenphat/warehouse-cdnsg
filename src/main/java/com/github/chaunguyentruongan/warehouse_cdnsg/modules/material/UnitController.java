package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;

    @GetMapping
    public ResponseEntity<?> get(@RequestParam Long id) {
        Unit unit = unitService.findById(id);
        return ResponseEntity.ok(unit);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(unitService.findAllUnit());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(unitService.create(request));
    }

    @PutMapping
    public ResponseEntity<?> update(@RequestBody UnitRequest request, @RequestParam Long id) {
        return ResponseEntity.ok(unitService.updateName(id, request));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam Long id) {
        unitService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
