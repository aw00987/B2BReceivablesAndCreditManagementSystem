package com.aw00987.rcms.entity;

import com.aw00987.rcms.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 請求書
 */
@Data
@Entity
@Table(name = "invoices")
public class Invoice {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 請求書番号
     */
    @Column(name = "invoice_no", nullable = false, unique = true, length = 50)
    private String invoiceNo;

    /**
     * 取引先コード
     */
    @Column(name = "company_code", nullable = false)
    private String companyCode;

    /**
     * 請求金額
     */
    @Column(name = "invoice_amount", nullable = false, precision = 18)
    private BigDecimal invoiceAmount;

    /**
     * 発行日
     */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /**
     * 支払期日
     */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /**
     * 請求書作成者ユーサー名
     */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    /**
     * 備考
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    /**
     * 元金金额
     */
    @Column(name = "principal_amount", nullable = false, precision = 18)
    private BigDecimal principalAmount;

    /**
     * 延滞利息起算開始日
     */
    @Column(name = "interest_start_date")
    private LocalDate interestStartDate;

    /**
     * 利息
     */
    @Column(name = "interest_amount", precision = 18)
    private BigDecimal interestAmount;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
