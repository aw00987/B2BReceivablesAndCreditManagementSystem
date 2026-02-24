package com.aw00987.rcms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatisticsDto {
    private BigDecimal totalReceivableAmount;
    private BigDecimal overdueAmount;
    private BigDecimal collectionRate;
}
