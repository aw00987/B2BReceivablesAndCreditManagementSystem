package com.aw00987.rcms.enums;

/**
 * ユーザー役割
 */
public enum UserRole {

    ADMIN,

    SALES,

    LEGAL,

    FINANCE;

    public static UserRole fromName(String userRole) {
        for (UserRole value : UserRole.values()) {
            if (value.name().equalsIgnoreCase(userRole)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid user role: " + userRole);
    }

}
