package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_receipt;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UniformReceiptRequest {
    private LocalDate date;
    private String cusName;
    private List<Detail> details;

    @Getter
    @Setter
    public static class Detail {
        private Long uniformId;
        private Long quantity;
    }
}