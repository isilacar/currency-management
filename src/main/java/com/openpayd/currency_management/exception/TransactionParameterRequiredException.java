package com.openpayd.currency_management.exception;

public class TransactionParameterRequiredException extends RuntimeException {
    public TransactionParameterRequiredException(String message) {
        super(message);
    }
} 