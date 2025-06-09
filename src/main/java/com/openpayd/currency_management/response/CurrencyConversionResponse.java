package com.openpayd.currency_management.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyConversionResponse {
    private String baseCurrency;
    private String targetCurrency;
    private Double amount;
    private Double convertedAmount;
    private Double exchangeRate;
    private String transactionId;
    private LocalDate transactionDate;
} 