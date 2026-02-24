package com.aw00987.rcms.enums;

/**
 * ユーザー役割
 */
public enum UserRole {

    ADMIN("管理者"),

    SALES("営業マン"),

    LEGAL("法務担当"),

    FINANCE("財務担当");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
