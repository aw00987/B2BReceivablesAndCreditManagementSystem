package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.InvoiceQueryResponseDto;
import com.aw00987.rcms.entity.Invoice;
import com.aw00987.rcms.entity.Reconciliation;
import com.aw00987.rcms.enums.InvoiceStatus;
import com.aw00987.rcms.enums.MatchType;
import com.aw00987.rcms.repository.InvoiceMapper;
import com.aw00987.rcms.repository.ReconciliationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 入金消込に関連するビジネスロジックを提供するサービス。
 * 銀行取引データ（CSV）に基づいた自動消込処理などを担当します。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReconciliationService {

    private final ReconciliationMapper reconciliationMapper;

    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;

    /**
     * CSVファイルを解析し、請求書との自動消込を実行します。
     *
     * @param file アップロードされたCSVファイル
     * @return 消込結果の統計情報（マッチ件数、合計金額）
     * @throws RuntimeException CSV解析中にエラーが発生した場合
     */
    @Transactional
    public Map<String, Object> autoReconcile(MultipartFile file) {
        log.info("CSVファイルによる自動消込を開始します: {}", file.getOriginalFilename());

        int matchedCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // ヘッダーをスキップ
                }

                String[] columns = line.split(",");
                if (columns.length < 5) {
                    continue;
                }

                // CSVフォーマット想定: 銀行取引ID, 請求書番号, 振込依頼人名, 取引日, 取引金額
                String bankTransactionId = columns[0].trim();
                String invoiceNo = columns[1].trim();
                String payerName = columns[2].trim();
                LocalDate transactionDate = LocalDate.parse(columns[3].trim());
                BigDecimal transactionAmount = new BigDecimal(columns[4].trim());

                InvoiceQueryResponseDto invoice = invoiceMapper.selectByInvoiceNo(invoiceNo)
                        .orElseThrow();//todo:

                // すでに消込済みの場合はスキップ
                if (InvoiceStatus.fromName(invoice.getStatus()) == InvoiceStatus.PAID) {
                    continue;
                }

                Invoice updateInvoice = new Invoice();
                updateInvoice.setId(invoice.getId());

                // 消込情報の作成
                Reconciliation reconciliation = new Reconciliation();
                reconciliation.setInvoiceNo(invoice.getInvoiceNo());
                reconciliation.setBankTransactionId(bankTransactionId);
                reconciliation.setPayerName(payerName);
                reconciliation.setTransactionDate(transactionDate);
                reconciliation.setTransactionAmount(transactionAmount);
                reconciliation.setReconciliationAmount(transactionAmount);
                reconciliation.setReconciledAt(LocalDateTime.now());
                reconciliation.setReconciledBy("SYSTEM"); // 自動消込のためシステム
                reconciliation.setDescription("CSV自動消込");

                // 金額チェックとマッチングタイプの判定
                if (invoice.getInvoiceAmount().compareTo(transactionAmount) == 0) {
                    reconciliation.setMatchType(MatchType.EXACT);
                    reconciliation.setVarianceAmount(BigDecimal.ZERO);
                    updateInvoice.setStatus(InvoiceStatus.PAID);
                } else {
                    reconciliation.setMatchType(MatchType.AMOUNT_VARIANCE);
                    reconciliation.setVarianceAmount(invoice.getInvoiceAmount().subtract(transactionAmount));
                    // 差額がある場合でも、一旦ステータスを更新 todo:
                    updateInvoice.setStatus(InvoiceStatus.PAID);
                }

                reconciliationMapper.save(reconciliation);
                invoiceMapper.update(updateInvoice);

                matchedCount++;
                totalAmount = totalAmount.add(transactionAmount);

            }
        } catch (Exception e) {
            log.error("CSV解析中にエラーが発生しました", e);
            throw new RuntimeException("CSV解析エラー: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("matchedCount", matchedCount);
        result.put("totalAmount", totalAmount);

        log.info("自動消込が完了しました。マッチ件数: {}, 合計金額: {}", matchedCount, totalAmount);
        return result;
    }
}
