package com.openpayd.currency_management.mapper;

import com.openpayd.currency_management.client.response.CurrencyConversionApiResponse;
import com.openpayd.currency_management.client.response.ExchangeRateApiResponse;
import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.entity.CurrencyConverterEntity;
import com.openpayd.currency_management.request.CurrencyConversionRequest;
import com.openpayd.currency_management.response.CurrencyConversionResponse;
import com.openpayd.currency_management.response.CurrencyConverterHistoryPaginationResponse;
import com.openpayd.currency_management.response.CurrencyHistoryResponse;
import com.openpayd.currency_management.response.ExchangeRateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;
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
    @Mapping(target = "id", ignore = true)
    CurrencyConverterEntity toCurrencyConverterEntity(CurrencyConversionApiResponse currencyConversionApiResponse);

    CurrencyConversionDto toCurrencyConversionDto(CurrencyConverterEntity currencyConverterEntity);

    @Mapping(target = "currencyHistoryResponseList", expression = "java(mapToHistoryResponseList(currencyConverterEntityPage.getContent()))")
    @Mapping(target = "totalValue", source = "totalElements")
    @Mapping(target = "totalPages", source = "totalPages")
    @Mapping(target = "currentPage", source = "pageable.pageNumber")
    @Mapping(target = "viewedValueCount", source = "pageable.pageSize")
    CurrencyConverterHistoryPaginationResponse getCurrencyHistoryPagination(Page<CurrencyConverterEntity> currencyConverterEntityPage);

    @Mapping(target = "baseCurrency", source = "request.base")
    @Mapping(target = "targetCurrency", source = "request.target")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "convertedAmount", source = "response.result")
    @Mapping(target = "exchangeRate", source = "response.info.quote")
    @Mapping(target = "transactionId", expression = "java(generateTransactionId())")
    @Mapping(target = "transactionDate", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "id", ignore = true)
    CurrencyConverterEntity convertCurrency(CurrencyConversionRequest request, CurrencyConversionApiResponse response);


    CurrencyConversionResponse toCurrencyConversionResponse(CurrencyConverterEntity entity);


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

    default List<CurrencyHistoryResponse> mapToHistoryResponseList(List<CurrencyConverterEntity> entities) {
        return entities.stream()
                .map(entity -> new CurrencyHistoryResponse(
                        entity.getId(),
                        entity.getBaseCurrency(),
                        entity.getTargetCurrency(),
                        entity.getAmount(),
                        entity.getConvertedAmount(),
                        entity.getExchangeRate(),
                        entity.getTransactionId(),
                        entity.getTransactionDate()))
                .toList();
    }


} 