package com.aw00987.rcms.entity;

import com.aw00987.rcms.enums.MatchType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消込履歴
 */
@Data
@Entity
@Table(name = "reconciliations")
public class Reconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 請求書番号
     */
    @Column(name = "invoice_no", nullable = false)
    private String invoiceNo;

    /**
     * 銀行取引ID
     */
    @Column(name = "bank_transaction_id", nullable = false)
    private String bankTransactionId;

    /**
     * 振込依頼人名
     */
    @Column(name = "payer_name", nullable = false)
    private String payerName;
    /**
     * 取引日
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    /**
     * 取引金額
     */
    @Column(name = "transaction_amount", nullable = false, precision = 18)
    private BigDecimal transactionAmount;

    /**
     * 消込金額
     */
    @Column(name = "reconciliation_amount", nullable = false, precision = 18)
    private BigDecimal reconciliationAmount;

    /**
     * マッチングタイプ
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType matchType;

    /**
     * 摘要
     */
    @Column(name = "description", nullable = false)
    private String description;

    /**
     * 差額
     */
    @Column(name = "variance_amount", precision = 18)
    private BigDecimal varianceAmount;

    /**
     * 消込日時
     */
    @Column(name = "reconciled_at", nullable = false)
    private LocalDateTime reconciledAt;

    /**
     * 操作者
     */
    @Column(name = "reconciled_by", nullable = false)
    private String reconciledBy;
}
