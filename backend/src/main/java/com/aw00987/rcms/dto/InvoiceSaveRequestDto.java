package com.aw00987.rcms.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

//todo：入参校验
@Data
public class InvoiceSaveRequestDto {
    private String invoiceNo;
    private String companyId;
    private BigDecimal invoiceAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String notes;
    private String createdByUserId;
}