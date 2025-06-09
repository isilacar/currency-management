package com.openpayd.currency_management.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExchangeRateResponse {
    private String baseCurrency;
    private String targetCurrency;
    private Double exchangeRate;


}
