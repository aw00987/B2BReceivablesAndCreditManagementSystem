package com.aw00987.rcms.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceRequestDto {
    private String companyCode;
    private BigDecimal invoiceAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String notes;
    private String createdBy;
}