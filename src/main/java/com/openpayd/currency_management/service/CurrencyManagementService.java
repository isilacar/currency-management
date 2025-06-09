package com.openpayd.currency_management.service;


import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.request.CurrencyConversionRequest;
import com.openpayd.currency_management.request.CurrencyHistoryRequest;
import com.openpayd.currency_management.request.ExchangeRateRequest;
import com.openpayd.currency_management.response.CurrencyConverterHistoryPaginationResponse;
import com.openpayd.currency_management.response.ExchangeRateResponse;

public interface CurrencyManagementService {
    ExchangeRateResponse getExchangeRate(ExchangeRateRequest exchangeRateRequest);

    CurrencyConversionDto currencyConvert(CurrencyConversionRequest currencyConversionRequest);

    CurrencyConverterHistoryPaginationResponse getConversionHistory(CurrencyHistoryRequest currencyHistoryRequest);


}
