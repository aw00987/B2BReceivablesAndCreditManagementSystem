package com.aw00987.rcms.enums;

/**
 * ユーザーステータス
 */
public enum UserStatus {

    DISABLED,

    ENABLED;

    public static UserStatus fromName(String userStatus) {
        for (UserStatus value : UserStatus.values()) {
            if (value.name().equalsIgnoreCase(userStatus)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid user status: " + userStatus);
    }
}
