package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.DashboardStatisticsDto;
import com.aw00987.rcms.dto.InvoiceQueryRequestDto;
import com.aw00987.rcms.dto.InvoiceSaveRequestDto;
import com.aw00987.rcms.dto.InvoiceQueryResponseDto;
import com.aw00987.rcms.entity.Invoice;
import com.aw00987.rcms.enums.InvoiceStatus;
import com.aw00987.rcms.repository.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 請求書管理に関連するビジネスロジックを提供するサービス。
 * 請求書の作成、ステータス変更、遅延損害金の計算、統計情報の取得などを担当します。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceMapper invoiceMapper;

    public Page<InvoiceQueryResponseDto> pageInvoices(Pageable pageable, InvoiceQueryRequestDto queryParams) {
        List<InvoiceQueryResponseDto> list = invoiceMapper.selectPage(
                queryParams.getInvoiceNo(),
                queryParams.getCompanyName(),
                queryParams.getInvoiceStatus().name(),
                queryParams.getInvoiceAmount(),
                queryParams.getInvoiceDateFrom(),
                queryParams.getInvoiceDateTo(),
                queryParams.getCreateByUserRealName(),
                pageable.getOffset(), pageable.getPageSize()
        );
        long totalCount = invoiceMapper.selectCount(
                queryParams.getInvoiceNo(),
                queryParams.getCompanyName(),
                queryParams.getInvoiceStatus().name(),
                queryParams.getInvoiceAmount(),
                queryParams.getInvoiceDateFrom(),
                queryParams.getInvoiceDateTo(),
                queryParams.getCreateByUserRealName()
        );
        return new PageImpl<>(list, pageable, totalCount);
    }

    public InvoiceQueryResponseDto getInvoiceDetail(long id) {
        return invoiceMapper.selectById(id).orElse(null);
    }

    @Transactional
    public void addNewInvoice(InvoiceSaveRequestDto dto) {
        String invoiceNo = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        //todo: 检查日期必须在今天之后
        Invoice invoice = new Invoice();
        invoice.setInvoiceNo(invoiceNo);
        invoice.setCompanyId(dto.getCompanyId());
        invoice.setStatus(InvoiceStatus.NORMAL);//todo:检查直接映射数据库数据的可行性
        invoice.setInvoiceAmount(dto.getInvoiceAmount());
        invoice.setPrincipalAmount(dto.getInvoiceAmount());
        invoice.setInterestAmount(dto.getInvoiceAmount());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setNotes(dto.getNotes());
        invoice.setCreatedByUserId(dto.getCreatedByUserId());

        invoiceMapper.insert(invoice);
        System.out.println(invoice.getId());//todo:验证
    }

    //todo：前端注意控制只传改变了的字段，需要一个机制区分“可改变的”和“本次改变了的”
    @Transactional
    public void updateInvoiceById(long id,//todo:问ai应该用long还是Long
                                  InvoiceSaveRequestDto dto) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setInvoiceNo(dto.getInvoiceNo());
        invoice.setCompanyId(dto.getCompanyId());
        invoice.setStatus(InvoiceStatus.NORMAL);
        invoice.setInvoiceAmount(dto.getInvoiceAmount());
        invoice.setPrincipalAmount(dto.getInvoiceAmount());
        invoice.setInterestAmount(dto.getInvoiceAmount());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setNotes(dto.getNotes());
        invoice.setCreatedByUserId(dto.getCreatedByUserId());
        invoiceMapper.update(invoice);
    }

    @Transactional
    public void invoicePaid(long id, String note) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setStatus(InvoiceStatus.PAID);//todo: 好像不应该这么直接SET
        invoice.setNotes(StringUtils.trimToEmpty(note));
        invoiceMapper.update(invoice);
    }

    @Transactional
    public void invoiceDunning(long id, String note) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setStatus(InvoiceStatus.DUNNING);
        invoice.setNotes(StringUtils.trimToEmpty(note));
        invoice.setInterestStartDate(LocalDate.now());
        invoice.setInterestAmount(BigDecimal.ZERO);
        invoiceMapper.update(invoice);
    }

    @Transactional
    public void invoiceLitigation(long id, String note) {
        Invoice invoice = new Invoice();
        invoice.setId(id);
        invoice.setStatus(InvoiceStatus.LITIGATION);
        invoice.setNotes(StringUtils.trimToEmpty(note));
        invoiceMapper.update(invoice);
    }

    /**
     * 支払期限を過ぎた請求書を自動的に「延滞」ステータスに更新します（毎日0時に実行）。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional//todo：需要回滚到什么程度？事务的嵌套如何管理？
    public void invoiceOverdue() {
        List<Long> invoiceIds = invoiceMapper.selectInvoiceIdsNeedsToOverdue();
        for (Long invoiceId : invoiceIds) {
            Invoice invoice = new Invoice();
            invoice.setId(invoiceId);
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceMapper.update(invoice);
        }
    }

    /**
     * 遅延損害金の年利率（6%）
     */
    public static final BigDecimal INTEREST_RATE = new BigDecimal("0.06");

    /**
     * 1年間の日数
     */
    public static final BigDecimal DAYS_IN_YEAR = new BigDecimal(365);

    /**
     * 督促中の請求書に対して遅延損害金を計算し、加算します（毎日0時に実行）。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void calculateAllInvoiceInterest() {
        // 遅延損害金の計算が必要な請求書を取得
        List<Long> invoiceIds = invoiceMapper.selectInvoiceIdsNeedsToIncreaseInterest();

        for (Long invoiceId : invoiceIds) {
            InvoiceQueryResponseDto invoice = invoiceMapper.selectById(invoiceId).orElseThrow();

            log.debug("請求書番号 {} の遅延損害金を計算中...", invoice.getInvoiceNo());

            // 日本の商法/民法に基づき年利で計算。公式：請求残高 × (利率% ÷ 365)
            // 端数処理：四捨五入（HALF_UP）
            BigDecimal principalAmount = defaultZero(invoice.getPrincipalAmount());
            BigDecimal accumulatedInterest = principalAmount.multiply(INTEREST_RATE)
                    .divide(DAYS_IN_YEAR, 0, RoundingMode.HALF_UP);

            Invoice updateInvoice = new Invoice();
            updateInvoice.setId(invoice.getId());
            updateInvoice.setInterestAmount(defaultZero(invoice.getInterestAmount()).add(accumulatedInterest));
            updateInvoice.setInvoiceAmount(defaultZero(invoice.getInvoiceAmount()).add(accumulatedInterest));

            invoiceMapper.update(updateInvoice);

            log.debug("請求書番号 {} の遅延損害金を計算完了", invoice.getInvoiceNo());
        }
    }

    /**
     * todo: 搞清楚这个方法在干什么
     */
    public DashboardStatisticsDto getDashboardStatistics() {
        // 売掛金総額: PAID 以外のすべての請求書の合計
        List<String> receivableStatuses = List.of(
                InvoiceStatus.NORMAL.name(),
                InvoiceStatus.OVERDUE.name(),
                InvoiceStatus.DUNNING.name(),
                InvoiceStatus.LITIGATION.name()
        );
        BigDecimal totalReceivable = defaultZero(invoiceMapper.sumInvoiceAmountByStatuses(receivableStatuses));

        // 延滞金額: OVERDUE, DUNNING, LITIGATION の合計
        List<String> overdueStatuses = List.of(
                InvoiceStatus.OVERDUE.name(),
                InvoiceStatus.DUNNING.name(),
                InvoiceStatus.LITIGATION.name()
        );
        BigDecimal overdueAmount = defaultZero(invoiceMapper.sumInvoiceAmountByStatuses(overdueStatuses));

        // 回収率: PAID / (PAID + receivableStatuses)
        BigDecimal paidAmount = defaultZero(invoiceMapper.sumInvoiceAmountByStatuses(
                List.of(InvoiceStatus.PAID.name())
        ));

        BigDecimal totalAmount = paidAmount.add(totalReceivable);
        BigDecimal collectionRate = BigDecimal.ZERO;
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            collectionRate = paidAmount.divide(totalAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }

        return new DashboardStatisticsDto(totalReceivable, overdueAmount, collectionRate);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
