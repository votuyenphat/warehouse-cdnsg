package com.github.chaunguyentruongan.warehouse_cdnsg.exception;


import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseExceptionDTO {
    private LocalDate time;
    private int httpStatus;
    private String error;
    private String path;
}
