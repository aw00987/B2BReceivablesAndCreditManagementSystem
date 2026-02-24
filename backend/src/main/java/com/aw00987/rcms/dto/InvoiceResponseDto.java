package com.aw00987.rcms.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceResponseDto {
    private String invoiceNo;
    private String companyCode;
    private String companyName;
    private BigDecimal invoiceAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String createdBy;
    private String notes;
    private String status;
    private BigDecimal principalAmount;
    private LocalDate interestStartDate;
    private BigDecimal interestAmount;
}
