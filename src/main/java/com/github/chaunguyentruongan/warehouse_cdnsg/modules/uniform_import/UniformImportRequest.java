package com.github.chaunguyentruongan.warehouse_cdnsg.modules.uniform_import;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UniformImportRequest {
    private LocalDate date;
    private String note;
    private String nameResponse;
    private List<Detail> details;

    @Getter
    @Setter
    public static class Detail {
        private Long uniformId;
        private Long quantity;
    }
}