package com.openpayd.currency_management.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyHistoryResponse {

    private Long id;
    private String baseCurrency;
    private String targetCurrency;
    private double amount;
    private double convertedAmount;
    private double exchangeRate;
    private String transactionId;
    private LocalDate transactionDate;


}