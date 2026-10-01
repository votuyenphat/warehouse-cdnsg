package com.github.chaunguyentruongan.warehouse_cdnsg.modules.borrow_return;

import lombok.Data;
import java.util.List;

@Data
public class ManualEmailRequest {
    private List<String> toEmails;
    private String subject;
    private String content;
}
