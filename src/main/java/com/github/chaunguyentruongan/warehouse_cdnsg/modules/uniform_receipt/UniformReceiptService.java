package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_receipt;

import com.github.chaunguyentruongan.warehouse_cdnsg.exception.ResourceNotFoundException;
import com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_core.UniformService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniformReceiptService {
    private final UniformReceiptRepository receiptRepository;
    private final UniformService uniformService;

    public UniformReceipt findById(Long id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found receipt id: " + id));
    }

    public Page<UniformReceipt> getAll(LocalDate from, LocalDate to, String cusName, Pageable page) {
        return receiptRepository.searchAndFilter(from, to, cusName, page);
    }

    @Transactional
    public UniformReceipt create(UniformReceiptRequest request) {
        UniformReceipt receipt = new UniformReceipt();
        receipt.setDate(request.getDate());
        receipt.setCusName(request.getCusName());

        long totalQty = 0L;
        List<UniformReceiptDetail> details = request.getDetails().stream().map(itemReq -> {
            uniformService.updateStock(itemReq.getUniformId(), -itemReq.getQuantity()); // Xuất: Trừ (-) kho
            return UniformReceiptDetail.builder()
                    .uniform(uniformService.findById(itemReq.getUniformId()))
                    .uniformReceipt(receipt)
                    .quantity(itemReq.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        // Tự động tính tổng số lượng
        for (UniformReceiptDetail d : details) {
            totalQty += d.getQuantity();
        }
        receipt.setTotalQuantity(totalQty);
        receipt.setDetails(details);

        return receiptRepository.save(receipt);
    }

    @Transactional
    public UniformReceipt update(Long id, UniformReceiptRequest request) {
        UniformReceipt existing = findById(id);

        for (UniformReceiptDetail oldItem : existing.getDetails()) {
            uniformService.updateStock(oldItem.getUniform().getId(), oldItem.getQuantity()); // Hoàn tác: Cộng (+)
        }
        existing.getDetails().clear();

        existing.setDate(request.getDate());
        existing.setCusName(request.getCusName());

        long totalQty = 0L;
        for (UniformReceiptRequest.Detail itemReq : request.getDetails()) {
            uniformService.updateStock(itemReq.getUniformId(), -itemReq.getQuantity()); // Áp dụng: Trừ (-)
            UniformReceiptDetail newItem = UniformReceiptDetail.builder()
                    .uniformReceipt(existing)
                    .uniform(uniformService.findById(itemReq.getUniformId()))
                    .quantity(itemReq.getQuantity())
                    .build();
            totalQty += itemReq.getQuantity();
            existing.getDetails().add(newItem);
        }

        existing.setTotalQuantity(totalQty);
        return receiptRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        UniformReceipt existing = findById(id);
        for (UniformReceiptDetail item : existing.getDetails()) {
            uniformService.updateStock(item.getUniform().getId(), item.getQuantity()); // Xóa phiếu xuất: Cộng (+) kho
        }
        receiptRepository.delete(existing);
    }
}