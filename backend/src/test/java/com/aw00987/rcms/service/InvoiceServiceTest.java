package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.DashboardStatisticsDto;
import com.aw00987.rcms.dto.InvoiceQueryRequestDto;
import com.aw00987.rcms.dto.InvoiceQueryResponseDto;
import com.aw00987.rcms.dto.InvoiceSaveRequestDto;
import com.aw00987.rcms.entity.Invoice;
import com.aw00987.rcms.enums.InvoiceStatus;
import com.aw00987.rcms.repository.InvoiceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceMapper invoiceMapper;

    @InjectMocks
    private InvoiceService invoiceService;

    @Test
    void pageInvoicesPassesQueryParamsAndReturnsPage() {
        Pageable pageable = PageRequest.of(2, 5);
        InvoiceQueryRequestDto query = queryParams();
        InvoiceQueryResponseDto first = invoiceResponse(11L, "INV-011", "NORMAL", "1000");
        InvoiceQueryResponseDto second = invoiceResponse(12L, "INV-012", "NORMAL", "2000");
        when(invoiceMapper.selectPage(
                "INV", "Alpha", "NORMAL", new BigDecimal("1000"),
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                "Tanaka", 10L, 5
        )).thenReturn(List.of(first, second));
        when(invoiceMapper.selectCount(
                "INV", "Alpha", "NORMAL", new BigDecimal("1000"),
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                "Tanaka"
        )).thenReturn(17L);

        Page<InvoiceQueryResponseDto> result = invoiceService.pageInvoices(pageable, query);

        assertThat(result.getContent()).containsExactly(first, second);
        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(17);
        assertThat(result.getTotalPages()).isEqualTo(4);
        verify(invoiceMapper).selectPage(
                "INV", "Alpha", "NORMAL", new BigDecimal("1000"),
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                "Tanaka", 10L, 5
        );
        verify(invoiceMapper).selectCount(
                "INV", "Alpha", "NORMAL", new BigDecimal("1000"),
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31),
                "Tanaka"
        );
    }

    @Test
    void pageInvoicesThrowsWhenInvoiceStatusIsMissing() {
        InvoiceQueryRequestDto query = new InvoiceQueryRequestDto();

        assertThatThrownBy(() -> invoiceService.pageInvoices(PageRequest.of(0, 10), query))
                .isInstanceOf(NullPointerException.class);
        verifyNoInteractions(invoiceMapper);
    }

    @Test
    void getInvoiceDetailReturnsInvoiceWhenFound() {
        InvoiceQueryResponseDto invoice = invoiceResponse(7L, "INV-007", "PAID", "900");
        when(invoiceMapper.selectById(7L)).thenReturn(Optional.of(invoice));

        InvoiceQueryResponseDto result = invoiceService.getInvoiceDetail(7L);

        assertThat(result).isSameAs(invoice);
        verify(invoiceMapper).selectById(7L);
    }

    @Test
    void getInvoiceDetailReturnsNullWhenMissing() {
        when(invoiceMapper.selectById(8L)).thenReturn(Optional.empty());

        InvoiceQueryResponseDto result = invoiceService.getInvoiceDetail(8L);

        assertThat(result).isNull();
        verify(invoiceMapper).selectById(8L);
    }

    @Test
    void addNewInvoiceGeneratesInvoiceNoAndMapsRequestToNormalInvoice() {
        InvoiceSaveRequestDto request = saveRequest("INV-IGNORED", "1500");

        invoiceService.addNewInvoice(request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).insert(invoiceCaptor.capture());
        Invoice inserted = invoiceCaptor.getValue();
        assertThat(inserted.getInvoiceNo()).startsWith("INV-");
        assertThat(inserted.getInvoiceNo()).hasSize("INV-yyyyMMddHHmmssSSS".length());
        assertThat(inserted.getId()).isNull();
        assertThat(inserted.getStatus()).isEqualTo(InvoiceStatus.NORMAL);
        assertInvoiceSaveFields(inserted, "1500");
    }

    @Test
    void updateInvoiceByIdMapsRequestAndForcesNormalStatus() {
        InvoiceSaveRequestDto request = saveRequest("INV-123", "2300");

        invoiceService.updateInvoiceById(123L, request);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(123L);
        assertThat(updated.getInvoiceNo()).isEqualTo("INV-123");
        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.NORMAL);
        assertInvoiceSaveFields(updated, "2300");
    }

    @Test
    void invoicePaidTrimsNullNoteToEmptyAndUpdatesPaidStatus() {
        invoiceService.invoicePaid(21L, null);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(21L);
        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(updated.getNotes()).isEmpty();
    }

    @Test
    void invoicePaidTrimsNoteAndUpdatesPaidStatus() {
        invoiceService.invoicePaid(22L, "  paid by transfer  ");

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(22L);
        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(updated.getNotes()).isEqualTo("paid by transfer");
    }

    @Test
    void invoiceDunningSetsTodayInterestStartDateZeroInterestAndTrimmedNote() {
        LocalDate today = LocalDate.now();

        invoiceService.invoiceDunning(31L, "  reminder sent  ");

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(31L);
        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.DUNNING);
        assertThat(updated.getNotes()).isEqualTo("reminder sent");
        assertThat(updated.getInterestStartDate()).isBetween(today, LocalDate.now());
        assertThat(updated.getInterestAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void invoiceDunningTrimsNullNoteToEmpty() {
        invoiceService.invoiceDunning(32L, null);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getNotes()).isEmpty();
    }

    @Test
    void invoiceLitigationTrimsNoteAndUpdatesLitigationStatus() {
        invoiceService.invoiceLitigation(41L, "  handed to legal  ");

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(41L);
        assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.LITIGATION);
        assertThat(updated.getNotes()).isEqualTo("handed to legal");
    }

    @Test
    void invoiceLitigationTrimsNullNoteToEmpty() {
        invoiceService.invoiceLitigation(42L, null);

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getValue().getNotes()).isEmpty();
    }

    @Test
    void invoiceOverdueUpdatesEachInvoiceNeedingOverdueStatus() {
        when(invoiceMapper.selectInvoiceIdsNeedsToOverdue()).thenReturn(List.of(51L, 52L));

        invoiceService.invoiceOverdue();

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).selectInvoiceIdsNeedsToOverdue();
        verify(invoiceMapper, org.mockito.Mockito.times(2)).update(invoiceCaptor.capture());
        assertThat(invoiceCaptor.getAllValues()).extracting(Invoice::getId).containsExactly(51L, 52L);
        assertThat(invoiceCaptor.getAllValues()).extracting(Invoice::getStatus)
                .containsExactly(InvoiceStatus.OVERDUE, InvoiceStatus.OVERDUE);
    }

    @Test
    void invoiceOverdueDoesNothingWhenNoInvoicesNeedUpdate() {
        when(invoiceMapper.selectInvoiceIdsNeedsToOverdue()).thenReturn(List.of());

        invoiceService.invoiceOverdue();

        verify(invoiceMapper).selectInvoiceIdsNeedsToOverdue();
        verifyNoMoreInteractions(invoiceMapper);
    }

    @Test
    void calculateAllInvoiceInterestAddsRoundedDailyInterestToInvoiceAndInterestAmounts() {
        when(invoiceMapper.selectInvoiceIdsNeedsToIncreaseInterest()).thenReturn(List.of(61L));
        InvoiceQueryResponseDto current = invoiceResponse(61L, "INV-061", "DUNNING", "10010");
        current.setPrincipalAmount(new BigDecimal("10000"));
        current.setInterestAmount(new BigDecimal("10"));
        when(invoiceMapper.selectById(61L)).thenReturn(Optional.of(current));

        invoiceService.calculateAllInvoiceInterest();

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).selectInvoiceIdsNeedsToIncreaseInterest();
        verify(invoiceMapper).selectById(61L);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(61L);
        assertThat(updated.getInterestAmount()).isEqualByComparingTo("12");
        assertThat(updated.getInvoiceAmount()).isEqualByComparingTo("10012");
    }

    @Test
    void calculateAllInvoiceInterestDefaultsNullAmountsToZero() {
        when(invoiceMapper.selectInvoiceIdsNeedsToIncreaseInterest()).thenReturn(List.of(62L));
        InvoiceQueryResponseDto current = invoiceResponse(62L, "INV-062", "DUNNING", "0");
        current.setPrincipalAmount(null);
        current.setInterestAmount(null);
        current.setInvoiceAmount(null);
        when(invoiceMapper.selectById(62L)).thenReturn(Optional.of(current));

        invoiceService.calculateAllInvoiceInterest();

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper).update(invoiceCaptor.capture());
        Invoice updated = invoiceCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(62L);
        assertThat(updated.getInterestAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(updated.getInvoiceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateAllInvoiceInterestProcessesMultipleInvoicesInOrder() {
        when(invoiceMapper.selectInvoiceIdsNeedsToIncreaseInterest()).thenReturn(List.of(63L, 64L));
        InvoiceQueryResponseDto first = invoiceResponse(63L, "INV-063", "DUNNING", "3650");
        first.setPrincipalAmount(new BigDecimal("3650"));
        first.setInterestAmount(BigDecimal.ZERO);
        InvoiceQueryResponseDto second = invoiceResponse(64L, "INV-064", "DUNNING", "7300");
        second.setPrincipalAmount(new BigDecimal("7300"));
        second.setInterestAmount(new BigDecimal("5"));
        when(invoiceMapper.selectById(63L)).thenReturn(Optional.of(first));
        when(invoiceMapper.selectById(64L)).thenReturn(Optional.of(second));

        invoiceService.calculateAllInvoiceInterest();

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceMapper, org.mockito.Mockito.times(2)).update(invoiceCaptor.capture());
        List<Invoice> updates = invoiceCaptor.getAllValues();
        assertThat(updates).extracting(Invoice::getId).containsExactly(63L, 64L);
        assertThat(updates.get(0).getInterestAmount()).isEqualByComparingTo("1");
        assertThat(updates.get(0).getInvoiceAmount()).isEqualByComparingTo("3651");
        assertThat(updates.get(1).getInterestAmount()).isEqualByComparingTo("6");
        assertThat(updates.get(1).getInvoiceAmount()).isEqualByComparingTo("7301");
    }

    @Test
    void calculateAllInvoiceInterestThrowsWhenInvoiceMissing() {
        when(invoiceMapper.selectInvoiceIdsNeedsToIncreaseInterest()).thenReturn(List.of(65L));
        when(invoiceMapper.selectById(65L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> invoiceService.calculateAllInvoiceInterest())
                .isInstanceOf(java.util.NoSuchElementException.class);
        verify(invoiceMapper).selectInvoiceIdsNeedsToIncreaseInterest();
        verify(invoiceMapper).selectById(65L);
        verifyNoMoreInteractions(invoiceMapper);
    }

    @Test
    void calculateAllInvoiceInterestDoesNothingWhenNoInvoicesNeedUpdate() {
        when(invoiceMapper.selectInvoiceIdsNeedsToIncreaseInterest()).thenReturn(List.of());

        invoiceService.calculateAllInvoiceInterest();

        verify(invoiceMapper).selectInvoiceIdsNeedsToIncreaseInterest();
        verifyNoMoreInteractions(invoiceMapper);
    }

    @Test
    void getDashboardStatisticsCalculatesReceivableOverdueAndCollectionRate() {
        when(invoiceMapper.sumInvoiceAmountByStatuses(List.of("NORMAL", "OVERDUE", "DUNNING", "LITIGATION")))
                .thenReturn(new BigDecimal("300"));
        when(invoiceMapper.sumInvoiceAmountByStatuses(List.of("OVERDUE", "DUNNING", "LITIGATION")))
                .thenReturn(new BigDecimal("120"));
        when(invoiceMapper.sumInvoiceAmountByStatuses(List.of("PAID")))
                .thenReturn(new BigDecimal("700"));

        DashboardStatisticsDto result = invoiceService.getDashboardStatistics();

        assertThat(result.getTotalReceivableAmount()).isEqualByComparingTo("300");
        assertThat(result.getOverdueAmount()).isEqualByComparingTo("120");
        assertThat(result.getCollectionRate()).isEqualByComparingTo("70.0000");
        verify(invoiceMapper).sumInvoiceAmountByStatuses(List.of("NORMAL", "OVERDUE", "DUNNING", "LITIGATION"));
        verify(invoiceMapper).sumInvoiceAmountByStatuses(List.of("OVERDUE", "DUNNING", "LITIGATION"));
        verify(invoiceMapper).sumInvoiceAmountByStatuses(List.of("PAID"));
    }

    @Test
    void getDashboardStatisticsDefaultsNullSumsToZero() {
        when(invoiceMapper.sumInvoiceAmountByStatuses(List.of("NORMAL", "OVERDUE", "DUNNING", "LITIGATION")))
                .thenReturn(null);
        when(invoiceMapper.sumInvoiceAmountByStatuses(List.of("OVERDUE", "DUNNING", "LITIGATION")))
                .thenReturn(null);
        when(invoiceMapper.sumInvoiceAmountByStatuses(List.of("PAID")))
                .thenReturn(null);

        DashboardStatisticsDto result = invoiceService.getDashboardStatistics();

        assertThat(result.getTotalReceivableAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getOverdueAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getCollectionRate()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private static InvoiceQueryRequestDto queryParams() {
        InvoiceQueryRequestDto dto = new InvoiceQueryRequestDto();
        dto.setInvoiceNo("INV");
        dto.setCompanyName("Alpha");
        dto.setInvoiceStatus("NORMAL");
        dto.setInvoiceAmount(new BigDecimal("1000"));
        dto.setInvoiceDateFrom(LocalDate.of(2026, 5, 1));
        dto.setInvoiceDateTo(LocalDate.of(2026, 5, 31));
        dto.setCreateByUserRealName("Tanaka");
        return dto;
    }

    private static InvoiceSaveRequestDto saveRequest(String invoiceNo, String invoiceAmount) {
        InvoiceSaveRequestDto dto = new InvoiceSaveRequestDto();
        dto.setInvoiceNo(invoiceNo);
        dto.setCompanyId("company-1");
        dto.setInvoiceAmount(new BigDecimal(invoiceAmount));
        dto.setIssueDate(LocalDate.of(2026, 5, 10));
        dto.setDueDate(LocalDate.of(2026, 6, 10));
        dto.setNotes("initial note");
        dto.setCreatedByUserId("user-1");
        return dto;
    }

    private static void assertInvoiceSaveFields(Invoice invoice, String amount) {
        assertThat(invoice.getCompanyId()).isEqualTo("company-1");
        assertThat(invoice.getInvoiceAmount()).isEqualByComparingTo(amount);
        assertThat(invoice.getPrincipalAmount()).isEqualByComparingTo(amount);
        assertThat(invoice.getInterestAmount()).isEqualByComparingTo(amount);
        assertThat(invoice.getIssueDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(invoice.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 10));
        assertThat(invoice.getNotes()).isEqualTo("initial note");
        assertThat(invoice.getCreatedByUserId()).isEqualTo("user-1");
    }

    private static InvoiceQueryResponseDto invoiceResponse(long id, String invoiceNo, String status, String amount) {
        InvoiceQueryResponseDto dto = new InvoiceQueryResponseDto();
        dto.setId(id);
        dto.setInvoiceNo(invoiceNo);
        dto.setStatus(status);
        dto.setInvoiceAmount(new BigDecimal(amount));
        dto.setPrincipalAmount(new BigDecimal(amount));
        dto.setInterestAmount(BigDecimal.ZERO);
        return dto;
    }
}
