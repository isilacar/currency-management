package com.openpayd.currency_management.mapper;

import com.openpayd.currency_management.client.response.CurrencyConversionApiResponse;
import com.openpayd.currency_management.client.response.ExchangeRateApiResponse;
import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.entity.CurrencyConverterEntity;
import com.openpayd.currency_management.response.ExchangeRateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ExchangeMapper {

    @Mapping(target = "baseCurrency", source = "source")
    @Mapping(target = "targetCurrency", expression = "java(getTargetCurrency(exchangeRateApiResponse.getQuotes()))")
    @Mapping(target = "exchangeRate", source = "quotes", qualifiedByName = "getRate")
    ExchangeRateResponse toExchangeRateResponse(ExchangeRateApiResponse exchangeRateApiResponse);


    @Mapping(target = "baseCurrency", source = "query.from")
    @Mapping(target = "targetCurrency", source = "query.to")
    @Mapping(target = "amount", source = "query.amount")
    @Mapping(target = "convertedAmount", source = "result")
    @Mapping(target = "exchangeRate", source = "info.quote")
    @Mapping(target = "transactionId", expression = "java(generateTransactionId())")
    @Mapping(target = "transactionDate", expression = "java(java.time.LocalDate.now())")
    CurrencyConverterEntity toCurrencyConverterEntity(CurrencyConversionApiResponse currencyConversionApiResponse);

    CurrencyConversionDto toCurrencyConversionDto(CurrencyConverterEntity currencyConverterEntity);

    @Named("getRate")
    default Double getRate(Map<String, Double> quotes) {
        return quotes != null ? quotes.values().stream().findFirst().orElse(null) : null;
    }

    default String getTargetCurrency(Map<String, Double> quotes) {
        if (quotes == null || quotes.isEmpty()) {
            return null;
        }
        String key = quotes.keySet().iterator().next();
        return key.substring(3); // first three character refers to base currency, next three character refers to  target currency
    }

    default String generateTransactionId() {
        return UUID.randomUUID().toString();
    }




} 