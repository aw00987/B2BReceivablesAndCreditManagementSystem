package com.aw00987.rcms.dto;

import com.aw00987.rcms.enums.InvoiceStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceQueryRequestDto {
    @Size(max = 50)
    private String invoiceNo;//like
    @Size(max = 100)
    private String companyName;//like

    private InvoiceStatus invoiceStatus;//equals

    @DecimalMin(value = "0.00")
    @Digits(integer = 12, fraction = 2)//todo:
    private BigDecimal invoiceAmount;//bigger than

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate invoiceDateFrom;//between
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate invoiceDateTo;//between

    @Size(max = 100)
    private String createByUserRealName;//specific

    @AssertTrue
    public boolean isInvoiceDateRangeValid() {
        return invoiceDateFrom == null || invoiceDateTo == null
                || !invoiceDateFrom.isAfter(invoiceDateTo);
    }

    public void setCompanyName(String companyName) {
        this.companyName = StringUtils.trimToNull(companyName);
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = InvoiceStatus.fromName(StringUtils.trimToNull(invoiceStatus));
    }

    public void setCreateByUserRealName(String createByUserRealName) {
        this.createByUserRealName = StringUtils.trimToNull(createByUserRealName);
    }
}
