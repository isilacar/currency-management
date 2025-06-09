package com.openpayd.currency_management.service.impl;

import com.openpayd.currency_management.client.ExchangeClient;
import com.openpayd.currency_management.client.response.ExchangeRateApiResponse;
import com.openpayd.currency_management.enums.CurrencySymbol;
import com.openpayd.currency_management.exception.CurrencySymbolNotFoundException;
import com.openpayd.currency_management.exception.CurrencySymbolNullException;
import com.openpayd.currency_management.mapper.ExchangeMapper;
import com.openpayd.currency_management.request.ExchangeRateRequest;
import com.openpayd.currency_management.response.ExchangeRateResponse;
import com.openpayd.currency_management.service.CurrencyManagementService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CurrencyManagementServiceImpl implements CurrencyManagementService {

    private final ExchangeClient exchangeClient;
    private final ExchangeMapper exchangeMapper;
    private static final Logger logger = LogManager.getLogger(CurrencyManagementServiceImpl.class);

    @Value("${currency-layer.api.key}")
    private String apiKey;

    @Cacheable(value = "exchangeRates", key = "T(java.lang.String).valueOf(#exchangeRateRequest.base).toUpperCase() + '-' + T(java.lang.String).valueOf(#exchangeRateRequest.target).toUpperCase()")
    public ExchangeRateResponse getExchangeRate(ExchangeRateRequest exchangeRateRequest) {
        logger.info("Fetching data from API: {}-{}", 
            exchangeRateRequest.getBase(), 
            exchangeRateRequest.getTarget());

        if(Objects.isNull(exchangeRateRequest.getBase()) || Objects.isNull(exchangeRateRequest.getTarget())) {
            logger.error("Currency symbols are null. Base: {}, Target: {}", 
                exchangeRateRequest.getBase(), 
                exchangeRateRequest.getTarget());
            throw new CurrencySymbolNullException("Currency symbols can not be null. Please enter valid currency codes: "+ Arrays.toString(CurrencySymbol.values()));
        }

        String base = exchangeRateRequest.getBase().toUpperCase();
        String target = exchangeRateRequest.getTarget().toUpperCase();

        validateCurrencySymbol(base, "base");
        validateCurrencySymbol(target, "target");

        logger.debug("Making API call to get exchange rate for {}-{}", base, target);
        ExchangeRateApiResponse exchangeRateApiResponse = exchangeClient.getExchangeRate(apiKey, base, target);
        ExchangeRateResponse response = exchangeMapper.toExchangeRateResponse(exchangeRateApiResponse);
        
        logger.info("Data stored in cache: {}-{}", base, target);
        return response;
    }

    private void validateCurrencySymbol(String symbol, String type) {
        try {
            CurrencySymbol.valueOf(symbol);
            logger.debug("Validated {} currency symbol: {}", type, symbol);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid {} currency symbol: {}", type, symbol);
            throw new CurrencySymbolNotFoundException(
                    String.format("Invalid %s currency symbol: %s. Valid currency codes: %s",
                            type,
                            symbol,
                            Arrays.toString(CurrencySymbol.values()))
            );
        }
    }

}





