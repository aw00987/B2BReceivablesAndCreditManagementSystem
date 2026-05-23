package com.aw00987.rcms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InvoiceStatus {
    NORMAL,
    PAID,
    OVERDUE,
    DUNNING,
    LITIGATION;

    public static InvoiceStatus fromName(String name) {
        for (InvoiceStatus value : InvoiceStatus.values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid invoice status name: " + name);
    }

    public boolean canTransitTo(InvoiceStatus target) {
        return switch (this) {
            case NORMAL -> target == OVERDUE || target == PAID;
            case OVERDUE -> target == DUNNING || target == PAID;
            case DUNNING -> target == LITIGATION || target == PAID;
            case LITIGATION -> target == PAID;
            case PAID -> false;
        };
    }
}
