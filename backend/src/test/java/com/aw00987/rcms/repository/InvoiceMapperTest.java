package com.aw00987.rcms.repository;

import com.aw00987.rcms.dto.InvoiceQueryResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class InvoiceMapperTest {

    @Autowired
    private InvoiceMapper invoiceMapper;

    @Test
    void selectPage() {
        List<InvoiceQueryResponseDto> invoices = invoiceMapper.selectPage(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                10
        );
        invoices.forEach(System.out::println);
    }
}
