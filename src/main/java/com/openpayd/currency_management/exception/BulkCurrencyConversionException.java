package com.openpayd.currency_management.exception;

import lombok.Getter;

@Getter
public class BulkCurrencyConversionException extends RuntimeException {
    private final String baseCurrency;
    private final String targetCurrency;
    private final Double amount;
    private final String errorCode;

    public BulkCurrencyConversionException(String message, String baseCurrency, String targetCurrency, Double amount, String errorCode) {
        super(message);
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.amount = amount;
        this.errorCode = errorCode;
    }


    public static BulkCurrencyConversionException invalidAmount(String baseCurrency, String targetCurrency, Double amount) {
        return new BulkCurrencyConversionException(
            String.format("Invalid amount: %s for conversion %s/%s", amount, baseCurrency, targetCurrency),
            baseCurrency,
            targetCurrency,
            amount,
            "INVALID_AMOUNT"
        );
    }

    public static BulkCurrencyConversionException invalidRequest(String baseCurrency, String targetCurrency, Double amount) {
        return new BulkCurrencyConversionException(
                String.format("Invalid request parameters. Base: %s, Target: %s, Amount: %s", baseCurrency, targetCurrency, amount),
                baseCurrency,
                targetCurrency,
                amount,
                "INVALID_REQUEST"
        );
    }

} 