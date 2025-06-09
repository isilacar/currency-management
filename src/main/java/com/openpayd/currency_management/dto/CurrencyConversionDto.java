package com.openpayd.currency_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyConversionDto implements Serializable {

    @Serial
    private static final long serialVersionUID = -5490278889365403254L;

    private Long id;
    private String baseCurrency;
    private String targetCurrency;
    private double amount;
    private double convertedAmount;
    private double exchangeRate;
    private String transactionId;
    private LocalDate transactionDate;
}
