package com.aw00987.rcms.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceQueryResponseDto {
    private long id;
    private String invoiceNo;
    private String status;
    private String companyName;
    private BigDecimal invoiceAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate interestStartDate;
    private String createdBy;
    private String notes;
}
