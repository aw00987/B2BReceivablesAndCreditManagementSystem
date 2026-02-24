package com.aw00987.rcms.controller;

import com.aw00987.rcms.dto.DashboardStatisticsDto;
import com.aw00987.rcms.dto.InvoiceRequestDto;
import com.aw00987.rcms.dto.InvoiceResponseDto;
import com.aw00987.rcms.entity.Invoice;
import com.aw00987.rcms.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody InvoiceRequestDto invoiceRequestDto) {
        return ResponseEntity.ok(invoiceService.createInvoice(invoiceRequestDto));
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponseDto>> getInvoices(Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getInvoices(pageable));
    }

    @GetMapping("/{invoiceNo}")
    public ResponseEntity<InvoiceResponseDto> getInvoice(@PathVariable String invoiceNo) {
        return ResponseEntity.ok(invoiceService.getInvoiceDetail(invoiceNo));
    }

    @PutMapping("/{invoiceNo}/dunning")
    public ResponseEntity<Invoice> invoiceDunning(@PathVariable String invoiceNo, @RequestParam(required = false) String note) {
        return ResponseEntity.ok(invoiceService.invoiceDunning(invoiceNo, note));
    }

    @PutMapping("/{invoiceNo}/litigation")
    public ResponseEntity<Invoice> invoiceLitigation(@PathVariable String invoiceNo, @RequestParam(required = false) String note) {
        return ResponseEntity.ok(invoiceService.invoiceLitigation(invoiceNo, note));
    }

    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsDto> getDashboardStatistics() {
        return ResponseEntity.ok(invoiceService.getDashboardStatistics());
    }

}
