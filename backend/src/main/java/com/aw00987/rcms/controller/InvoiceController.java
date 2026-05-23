package com.aw00987.rcms.controller;

import com.aw00987.rcms.dto.DashboardStatisticsDto;
import com.aw00987.rcms.dto.InvoiceQueryRequestDto;
import com.aw00987.rcms.dto.InvoiceSaveRequestDto;
import com.aw00987.rcms.dto.InvoiceQueryResponseDto;
import com.aw00987.rcms.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//todo：@RestControllerAdvice
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/create")
    public ResponseEntity<String> createInvoice(@RequestBody InvoiceSaveRequestDto invoiceSaveRequestDto) {
        invoiceService.addNewInvoice(invoiceSaveRequestDto);
        return ResponseEntity.ok("");
    }

    @PutMapping("/update/{id}")//todo: 询问ai最佳实践是什么？pathvarible还是放到dto里，用long还是Long
    public ResponseEntity<String> updateInvoice(Long id, @RequestBody InvoiceSaveRequestDto invoiceSaveRequestDto) {
        invoiceService.updateInvoiceById(id, invoiceSaveRequestDto);
        return ResponseEntity.ok("");
    }


    @GetMapping
    public ResponseEntity<Page<InvoiceQueryResponseDto>> getInvoices(
            Pageable pageable,
            @Valid @ModelAttribute InvoiceQueryRequestDto invoiceQueryRequestDto
    ) {

        return ResponseEntity.ok(invoiceService.pageInvoices(pageable, invoiceQueryRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceQueryResponseDto> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceDetail(id));
    }

    @PutMapping("/{invoiceNo}/dunning")
    public ResponseEntity<String> invoiceDunning(@PathVariable Long id, @RequestParam(required = false) String note) {
        invoiceService.invoiceDunning(id, note);
        return ResponseEntity.ok("");//todo:通用请求的构建
    }

    @PutMapping("/{invoiceNo}/litigation")
    public ResponseEntity<String> invoiceLitigation(@PathVariable Long id, @RequestParam(required = false) String note) {
        invoiceService.invoiceLitigation(id, note);
        return ResponseEntity.ok("");
    }

    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsDto> getDashboardStatistics() {
        return ResponseEntity.ok(invoiceService.getDashboardStatistics());
    }

}
