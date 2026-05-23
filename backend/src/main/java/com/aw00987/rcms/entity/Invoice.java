package com.aw00987.rcms.entity;

import com.aw00987.rcms.enums.InvoiceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 請求書
 */
@Data
public class Invoice {
    private Long id;
    private String invoiceNo;
    private String companyId;
    private InvoiceStatus status;
    private BigDecimal invoiceAmount;
    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private LocalDate interestStartDate;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String notes;
    private String createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
