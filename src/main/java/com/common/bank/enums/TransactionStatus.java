package com.common.bank.enums;

public enum TransactionStatus {
    PROCESSING("PROCESSING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private final String value;

    TransactionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
