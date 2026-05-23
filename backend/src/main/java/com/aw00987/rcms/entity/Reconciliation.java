package com.aw00987.rcms.entity;

import com.aw00987.rcms.enums.MatchType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Reconciliation {

    private Long id;

    private String invoiceNo;

    private String bankTransactionId;

    private String payerName;

    private LocalDate transactionDate;

    private BigDecimal transactionAmount;

    private BigDecimal reconciliationAmount;

    private MatchType matchType;

    private String description;

    private BigDecimal varianceAmount;

    private LocalDateTime reconciledAt;

    private String reconciledBy;
}
