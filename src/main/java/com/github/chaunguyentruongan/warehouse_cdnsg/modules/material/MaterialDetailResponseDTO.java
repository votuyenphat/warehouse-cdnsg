package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDetailResponseDTO {
    // Thông tin cơ bản của vật tư
    private Long id;
    private String name;
    private String unitName;
    private int inventory;

    // Lịch sử giao dịch
    private List<RecentTransactionDTO> importHistory;
    private List<RecentTransactionDTO> exportHistory;
}