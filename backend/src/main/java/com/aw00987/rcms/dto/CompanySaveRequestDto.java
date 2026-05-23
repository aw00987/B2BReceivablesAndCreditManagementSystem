package com.aw00987.rcms.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CompanySaveRequestDto {

    private Long id;

    private String companyCode;
    private String companyName;
    private String email;
    private String phoneNum;
    private String faxNum;
    private String address;

    private String creditRating;
    private BigDecimal creditLimit;

    private String picUserId;
}
