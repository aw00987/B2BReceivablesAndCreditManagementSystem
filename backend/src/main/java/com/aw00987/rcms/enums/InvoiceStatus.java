package com.aw00987.rcms.enums;

import lombok.Getter;

/**
 * 請求書ステータス
 */
@Getter
public enum InvoiceStatus {

    NORMAL("正常"),

    PAID("消込済"),

    OVERDUE("延滞"),

    DUNNING("督促中"),

    LITIGATION("法的措置");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }
}
