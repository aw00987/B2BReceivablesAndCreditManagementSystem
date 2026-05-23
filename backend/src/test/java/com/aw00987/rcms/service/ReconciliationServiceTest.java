package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.InvoiceQueryResponseDto;
import com.aw00987.rcms.entity.Invoice;
import com.aw00987.rcms.entity.Reconciliation;
import com.aw00987.rcms.enums.InvoiceStatus;
import com.aw00987.rcms.enums.MatchType;
import com.aw00987.rcms.repository.InvoiceMapper;
import com.aw00987.rcms.repository.ReconciliationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private ReconciliationMapper reconciliationMapper;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private ReconciliationService reconciliationService;

    @Test
    void autoReconcileCreatesExactMatchAndMarksInvoicePaid() {
        InvoiceQueryResponseDto invoice = invoice("INV-001", 101L, "NORMAL", "1200");
        when(invoiceMapper.selectByInvoiceNo("INV-001")).thenReturn(Optional.of(invoice));
        MultipartFile file = csv("""
                bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount
                BT-001,INV-001,Alpha Trading,2026-05-01,1200
                """);

        Map<String, Object> result = reconciliationService.autoReconcile(file);

        assertThat(result)
                .containsEntry("matchedCount", 1)
                .containsEntry("totalAmount", new BigDecimal("1200"));

        ArgumentCaptor<Reconciliation> reconciliationCaptor = ArgumentCaptor.forClass(Reconciliation.class);
        verify(reconciliationMapper).save(reconciliationCaptor.capture());
        Reconciliation saved = reconciliationCaptor.getValue();
        assertThat(saved.getInvoiceNo()).isEqualTo("INV-001");
        assertThat(saved.getBankTransactionId()).isEqualTo("BT-001");
        assertThat(saved.getPayerName()).isEqualTo("Alpha Trading");
        assertThat(saved.getTransactionDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(saved.getTransactionAmount()).isEqualByComparingTo("1200");
        assertThat(saved.getReconciliationAmount()).isEqualByComparingTo("1200");
        assertThat(saved.getMatchType()).isEqualTo(MatchType.EXACT);
        assertThat(saved.getVarianceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getReconciledAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(saved.getReconciledBy()).isEqualTo("SYSTEM");
        assertThat(saved.getDescription()).isEqualTo("CSV自動消込");

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(101L);
        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.PAID);
        verifyNoInteractions(invoiceService);
    }

    @Test
    void autoReconcileCreatesAmountVarianceMatchAndMarksInvoicePaid() {
        InvoiceQueryResponseDto invoice = invoice("INV-002", 102L, "OVERDUE", "1500");
        when(invoiceMapper.selectByInvoiceNo("INV-002")).thenReturn(Optional.of(invoice));
        MultipartFile file = csv("""
                bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount
                BT-002,INV-002,Beta Foods,2026-05-02,1300
                """);

        Map<String, Object> result = reconciliationService.autoReconcile(file);

        assertThat(result)
                .containsEntry("matchedCount", 1)
                .containsEntry("totalAmount", new BigDecimal("1300"));

        ArgumentCaptor<Reconciliation> reconciliationCaptor = ArgumentCaptor.forClass(Reconciliation.class);
        verify(reconciliationMapper).save(reconciliationCaptor.capture());
        Reconciliation saved = reconciliationCaptor.getValue();
        assertThat(saved.getMatchType()).isEqualTo(MatchType.AMOUNT_VARIANCE);
        assertThat(saved.getVarianceAmount()).isEqualByComparingTo("200");

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getId()).isEqualTo(102L);
        assertThat(invoiceCaptor.getValue().getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    void autoReconcileSkipsAlreadyPaidInvoiceWithoutSavingOrUpdating() {
        InvoiceQueryResponseDto invoice = invoice("INV-003", 103L, "PAID", "900");
        when(invoiceMapper.selectByInvoiceNo("INV-003")).thenReturn(Optional.of(invoice));
        MultipartFile file = csv("""
                bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount
                BT-003,INV-003,Gamma Retail,2026-05-03,900
                """);

        Map<String, Object> result = reconciliationService.autoReconcile(file);

        assertThat(result)
                .containsEntry("matchedCount", 0)
                .containsEntry("totalAmount", BigDecimal.ZERO);
        verify(invoiceMapper).selectByInvoiceNo("INV-003");
        verifyNoMoreInteractions(invoiceMapper);
        verifyNoInteractions(reconciliationMapper, invoiceService);
    }

    @Test
    void autoReconcileSkipsRowsWithTooFewColumns() {
        MultipartFile file = csv("""
                bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount
                BT-004,INV-004,Delta Store,2026-05-04
                """);

        Map<String, Object> result = reconciliationService.autoReconcile(file);

        assertThat(result)
                .containsEntry("matchedCount", 0)
                .containsEntry("totalAmount", BigDecimal.ZERO);
        verifyNoInteractions(invoiceMapper, reconciliationMapper, invoiceService);
    }

    @Test
    void autoReconcileReturnsZeroForHeaderOnlyFile() {
        MultipartFile file = csv("bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount\n");

        Map<String, Object> result = reconciliationService.autoReconcile(file);

        assertThat(result)
                .containsEntry("matchedCount", 0)
                .containsEntry("totalAmount", BigDecimal.ZERO);
        verifyNoInteractions(invoiceMapper, reconciliationMapper, invoiceService);
    }

    @Test
    void autoReconcileProcessesMultipleRowsAndAccumulatesTotalAmount() {
        InvoiceQueryResponseDto first = invoice("INV-005", 105L, "NORMAL", "1000");
        InvoiceQueryResponseDto second = invoice("INV-006", 106L, "DUNNING", "2500");
        when(invoiceMapper.selectByInvoiceNo("INV-005")).thenReturn(Optional.of(first));
        when(invoiceMapper.selectByInvoiceNo("INV-006")).thenReturn(Optional.of(second));
        MultipartFile file = csv("""
                bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount
                BT-005,INV-005,Epsilon Inc,2026-05-05,1000
                ignored,too,few,columns
                BT-006,INV-006,Zeta Corp,2026-05-06,2400
                """);

        Map<String, Object> result = reconciliationService.autoReconcile(file);

        assertThat(result)
                .containsEntry("matchedCount", 2)
                .containsEntry("totalAmount", new BigDecimal("3400"));

        ArgumentCaptor<Reconciliation> reconciliationCaptor = ArgumentCaptor.forClass(Reconciliation.class);
        verify(reconciliationMapper, org.mockito.Mockito.times(2)).save(reconciliationCaptor.capture());
        List<Reconciliation> saved = reconciliationCaptor.getAllValues();
        assertThat(saved).extracting(Reconciliation::getInvoiceNo).containsExactly("INV-005", "INV-006");
        assertThat(saved).extracting(Reconciliation::getMatchType)
                .containsExactly(MatchType.EXACT, MatchType.AMOUNT_VARIANCE);
        assertThat(saved.get(1).getVarianceAmount()).isEqualByComparingTo("100");

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper, org.mockito.Mockito.times(2)).update(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getAllValues()).extracting(Invoice::getId).containsExactly(105L, 106L);
        assertThat(invoiceCaptor.getAllValues()).extracting(Invoice::getStatus)
                .containsExactly(InvoiceStatus.PAID, InvoiceStatus.PAID);
    }

    @Test
    void autoReconcileWrapsMissingInvoiceErrorAndDoesNotSave() {
        when(invoiceMapper.selectByInvoiceNo("INV-MISSING")).thenReturn(Optional.empty());
        MultipartFile file = csv("""
                bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount
                BT-007,INV-MISSING,Eta Ltd,2026-05-07,700
                """);

        assertThatThrownBy(() -> reconciliationService.autoReconcile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CSV解析エラー");
        verify(invoiceMapper).selectByInvoiceNo("INV-MISSING");
        verifyNoMoreInteractions(invoiceMapper);
        verifyNoInteractions(reconciliationMapper, invoiceService);
    }

    @Test
    void autoReconcileWrapsInvalidDateErrorAndDoesNotQueryInvoice() {
        MultipartFile file = csv("""
                bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount
                BT-008,INV-008,Theta Ltd,not-a-date,800
                """);

        assertThatThrownBy(() -> reconciliationService.autoReconcile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CSV解析エラー");
        verifyNoInteractions(invoiceMapper, reconciliationMapper, invoiceService);
    }

    @Test
    void autoReconcileWrapsInvalidAmountErrorAndDoesNotQueryInvoice() {
        MultipartFile file = csv("""
                bank_transaction_id,invoice_no,payer_name,transaction_date,transaction_amount
                BT-009,INV-009,Iota Ltd,2026-05-09,not-a-number
                """);

        assertThatThrownBy(() -> reconciliationService.autoReconcile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CSV解析エラー");
        verifyNoInteractions(invoiceMapper, reconciliationMapper, invoiceService);
    }

    @Test
    void autoReconcileWrapsFileReadError() throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("broken.csv");
        when(file.getInputStream()).thenThrow(new IOException("read failed"));

        assertThatThrownBy(() -> reconciliationService.autoReconcile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CSV解析エラー: read failed");
        verifyNoInteractions(invoiceMapper, reconciliationMapper, invoiceService);
    }

    private static MockMultipartFile csv(String content) {
        return new MockMultipartFile(
                "file",
                "payments.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static InvoiceQueryResponseDto invoice(
            String invoiceNo,
            long id,
            String status,
            String invoiceAmount
    ) {
        InvoiceQueryResponseDto dto = new InvoiceQueryResponseDto();
        dto.setInvoiceNo(invoiceNo);
        dto.setId(id);
        dto.setStatus(status);
        dto.setInvoiceAmount(new BigDecimal(invoiceAmount));
        return dto;
    }
}
