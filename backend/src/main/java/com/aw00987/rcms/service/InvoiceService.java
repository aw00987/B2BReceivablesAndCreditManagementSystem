package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.DashboardStatisticsDto;
import com.aw00987.rcms.dto.InvoiceRequestDto;
import com.aw00987.rcms.dto.InvoiceResponseDto;
import com.aw00987.rcms.entity.Invoice;
import com.aw00987.rcms.enums.InvoiceStatus;
import com.aw00987.rcms.repository.CompanyRepository;
import com.aw00987.rcms.repository.InvoiceRepository;
import com.aw00987.rcms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 請求書管理に関連するビジネスロジックを提供するサービス。
 * 請求書の作成、ステータス変更、遅延損害金の計算、統計情報の取得などを担当します。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    /**
     * 新しい請求書を作成します。
     * @param invoiceRequestDto 請求書作成リクエスト情報
     * @return 保存された請求書エンティティ
     */
    @Transactional
    public Invoice createInvoice(InvoiceRequestDto invoiceRequestDto) {

        Invoice invoice = new Invoice();
        // 現在のタイムスタンプに基づいて請求書番号を生成
        String invoiceNo = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        invoice.setInvoiceNo(invoiceNo);
        invoice.setCompanyCode(invoiceRequestDto.getCompanyCode());
        invoice.setInvoiceAmount(invoiceRequestDto.getInvoiceAmount());
        invoice.setIssueDate(invoiceRequestDto.getIssueDate());
        invoice.setDueDate(invoiceRequestDto.getDueDate());
        invoice.setNotes(invoiceRequestDto.getNotes());
        invoice.setStatus(InvoiceStatus.NORMAL);
        invoice.setCreatedBy(invoiceRequestDto.getCreatedBy());
        invoice.setPrincipalAmount(invoiceRequestDto.getInvoiceAmount());
        invoice.setInterestAmount(BigDecimal.ZERO);

        return invoiceRepository.save(invoice);
    }

    /**
     * 請求書の消込（入金済み）処理を行います。
     * @param invoiceNo 請求書番号
     * @param note 備考
     * @return 更新された請求書エンティティ
     */
    @Transactional
    public Invoice invoicePaid(String invoiceNo, String note) {
        Invoice invoice = this.getInvoice(invoiceNo);
        invoice.setStatus(InvoiceStatus.PAID);
        if (StringUtils.isNotBlank(note)) {
            invoice.setNotes(note);
        }
        return invoiceRepository.save(invoice);
    }

    /**
     * 請求書の督促処理を行います（手動実行）。
     * @param invoiceNo 請求書番号
     * @param note 備考
     * @return 更新された請求書エンティティ
     */
    @Transactional
    public Invoice invoiceDunning(String invoiceNo, String note) {
        Invoice invoice = this.getInvoice(invoiceNo);
        invoice.setStatus(InvoiceStatus.DUNNING);
        if (StringUtils.isNotBlank(note)) {
            invoice.setNotes(note);
        }
        invoice.setInterestStartDate(LocalDate.now());
        invoice.setInterestAmount(BigDecimal.ZERO);
        return invoiceRepository.save(invoice);
    }

    /**
     * 請求書の法的措置（訴訟）処理を行います（手動実行）。
     * @param invoiceNo 請求書番号
     * @param note 備考
     * @return 更新された請求書エンティティ
     */
    @Transactional
    public Invoice invoiceLitigation(String invoiceNo, String note) {
        Invoice invoice = this.getInvoice(invoiceNo);
        invoice.setStatus(InvoiceStatus.LITIGATION);
        if (StringUtils.isNotBlank(note)) {
            invoice.setNotes(note);
        }
        return invoiceRepository.save(invoice);
    }

    /**
     * 支払期限を過ぎた請求書を自動的に「延滞」ステータスに更新します（毎日0時に実行）。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void invoiceOverdue() {
        List<String> invoiceNos = invoiceRepository
                .findInvoiceNosByStatusAndDueDateBefore(
                        InvoiceStatus.NORMAL, LocalDate.now()
                );
        for (String invoiceNo : invoiceNos) {
            Invoice invoice = this.getInvoice(invoiceNo);
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);
        }
    }

    /** 遅延損害金の年利率（6%） */
    public static final BigDecimal INTEREST_RATE = new BigDecimal("0.06");

    /** 1年間の日数 */
    public static final BigDecimal DAYS_IN_YEAR = new BigDecimal(365);

    /**
     * 督促中の請求書に対して遅延損害金を計算し、加算します（毎日0時に実行）。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void calculateAllInvoiceInterest() {
        // 遅延損害金の計算が必要な請求書を取得
        List<String> invoiceNos = invoiceRepository
                .findInvoiceNosByStatusAndInterestStartDateBefore(
                        InvoiceStatus.DUNNING, LocalDate.now()
                );

        for (String invoiceNo : invoiceNos) {
            log.debug("請求書番号 {} の遅延損害金を計算中...", invoiceNo);

            Invoice invoice = this.getInvoice(invoiceNo);

            // 日本の商法/民法に基づき年利で計算。公式：請求残高 × (利率% ÷ 365)
            // 端数処理：四捨五入（HALF_UP）
            BigDecimal principalAmount = invoice.getPrincipalAmount();
            BigDecimal accumulatedInterest = principalAmount.multiply(INTEREST_RATE)
                    .divide(DAYS_IN_YEAR, 0, RoundingMode.HALF_UP);

            invoice.setInterestAmount(invoice.getInterestAmount().add(accumulatedInterest));
            invoice.setInvoiceAmount(invoice.getInvoiceAmount().add(accumulatedInterest));

            invoiceRepository.save(invoice);

            log.debug("請求書番号 {} の遅延損害金を計算完了", invoiceNo);
        }
    }

    /**
     * 請求書の一覧をページングして取得します。
     * @param pageable ページング情報
     * @return 請求書レスポンスDTOのページ
     */
    public Page<InvoiceResponseDto> getInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(this::convertToResponseDto);
    }

    /**
     * 請求書の詳細情報を取得します。
     * @param invoiceNo 請求書番号
     * @return 請求書レスポンスDTO
     */
    public InvoiceResponseDto getInvoiceDetail(String invoiceNo) {
        return invoiceRepository.findByInvoiceNo(invoiceNo)
                .map(this::convertToResponseDto)
                .orElse(null);
    }

    /**
     * 請求書エンティティを取得します。
     * @param invoiceNo 請求書番号
     * @return 請求書エンティティ
     */
    public Invoice getInvoice(String invoiceNo) {
        return invoiceRepository.findByInvoiceNo(invoiceNo).orElse(null);
    }

    /**
     * ダッシュボード用の統計情報を取得します。
     * @return ダッシュボード統計DTO
     */
    public DashboardStatisticsDto getDashboardStatistics() {
        // 売掛金総額: PAID 以外のすべての請求書の合計
        List<InvoiceStatus> receivableStatuses = Arrays.asList(
                InvoiceStatus.NORMAL, InvoiceStatus.OVERDUE, InvoiceStatus.DUNNING, InvoiceStatus.LITIGATION
        );
        BigDecimal totalReceivable = invoiceRepository.findAll().stream()
                .filter(i -> receivableStatuses.contains(i.getStatus()))
                .map(Invoice::getInvoiceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 延滞金額: OVERDUE, DUNNING, LITIGATION の合計
        List<InvoiceStatus> overdueStatuses = Arrays.asList(
                InvoiceStatus.OVERDUE, InvoiceStatus.DUNNING, InvoiceStatus.LITIGATION
        );
        BigDecimal overdueAmount = invoiceRepository.findAll().stream()
                .filter(i -> overdueStatuses.contains(i.getStatus()))
                .map(Invoice::getInvoiceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 回収率: PAID / (PAID + receivableStatuses)
        BigDecimal paidAmount = invoiceRepository.findAll().stream()
                .filter(i -> i.getStatus() == InvoiceStatus.PAID)
                .map(Invoice::getInvoiceAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = paidAmount.add(totalReceivable);
        BigDecimal collectionRate = BigDecimal.ZERO;
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = paidAmount.divide(totalAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }

        return new DashboardStatisticsDto(totalReceivable, overdueAmount, collectionRate);
    }

    /**
     * 請求書エンティティをレスポンスDTOに変換します。
     * @param invoice 請求書エンティティ
     * @return 請求書レスポンスDTO
     */
    private InvoiceResponseDto convertToResponseDto(Invoice invoice) {
        InvoiceResponseDto dto = new InvoiceResponseDto();
        dto.setInvoiceNo(invoice.getInvoiceNo());
        dto.setCompanyCode(invoice.getCompanyCode());
        
        companyRepository.findByCompanyCode(invoice.getCompanyCode())
                .ifPresent(company -> dto.setCompanyName(company.getCompanyName()));
        
        dto.setInvoiceAmount(invoice.getInvoiceAmount());
        dto.setIssueDate(invoice.getIssueDate());
        dto.setDueDate(invoice.getDueDate());

        userRepository.findByUsername(invoice.getCreatedBy())
                .ifPresent(user -> dto.setCreatedBy(user.getRealName()));

        dto.setNotes(invoice.getNotes());
        dto.setStatus(invoice.getStatus().getLabel());
        dto.setPrincipalAmount(invoice.getPrincipalAmount());
        dto.setInterestStartDate(invoice.getInterestStartDate());
        dto.setInterestAmount(invoice.getInterestAmount());
        return dto;
    }
}
