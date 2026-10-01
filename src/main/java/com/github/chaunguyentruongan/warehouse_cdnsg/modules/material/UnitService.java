package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import java.util.List;

import org.springframework.stereotype.Service;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.exception.SqlDuplicateException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnitService {
    private final UnitRepository unitRepository;

    public Unit findById(Long id) {
        return unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found unit by id: " + id));
    }

    public List<Unit> findAllUnit() {
        return unitRepository.findAll();
    }

    public Unit create(UnitRequest request) {
        try {
            Unit unit = new Unit();
            unit.setName(request.getName());
            return unitRepository.save(unit);
        } catch (RuntimeException e) {
            throw new SqlDuplicateException(e.getMessage());
        }
    }

    @org.springframework.transaction.annotation.Transactional
    public Unit updateName(Long id, UnitRequest request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found unit by id: " + id));
        unit.setName(request.getName());
        return unitRepository.save(unit);
    }

    public void delete(Long id) {
        unitRepository.deleteById(id);
    }
}
