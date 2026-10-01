package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor // Bắt buộc phải có cái này để tự sinh constructor 8 tham số
public class RecentTransactionDTO {
    private Long materialId;
    private String materialName;
    private String unitName;
    private int quantity;
    private LocalDate transactionDate;

    private Long receiptId; // Tham số thứ 6 (Long)
    private String receiptCode; // Tham số thứ 7 (String)
    private String type; // Tham số thứ 8 (String)
}