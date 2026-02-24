package com.aw00987.rcms.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompanyResponseDto {
    private String companyCode;
    private String companyName;
    private String creditRating;
    private BigDecimal creditLimit;
    private String picUsername;
    private String picRealName;
    private String email;
    private String phoneNum;
    private String faxNum;
    private String address;
}
