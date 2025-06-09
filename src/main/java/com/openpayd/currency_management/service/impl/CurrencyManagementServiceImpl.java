package com.openpayd.currency_management.service.impl;

import com.openpayd.currency_management.client.ExchangeClient;
import com.openpayd.currency_management.client.response.CurrencyConversionApiResponse;
import com.openpayd.currency_management.client.response.ExchangeRateApiResponse;
import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.entity.CurrencyConverterEntity;
import com.openpayd.currency_management.enums.CurrencySymbol;
import com.openpayd.currency_management.exception.CurrencySymbolNotFoundException;
import com.openpayd.currency_management.exception.CurrencySymbolNullException;
import com.openpayd.currency_management.exception.TransactionHistoryNotFoundException;
import com.openpayd.currency_management.exception.TransactionParameterRequiredException;
import com.openpayd.currency_management.mapper.ExchangeMapper;
import com.openpayd.currency_management.repository.CurrencyManagementRepository;
import com.openpayd.currency_management.request.CurrencyConversionRequest;
import com.openpayd.currency_management.request.CurrencyHistoryRequest;
import com.openpayd.currency_management.request.ExchangeRateRequest;
import com.openpayd.currency_management.response.CurrencyConverterHistoryPaginationResponse;
import com.openpayd.currency_management.response.ExchangeRateResponse;
import com.openpayd.currency_management.service.CurrencyManagementService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CurrencyManagementServiceImpl implements CurrencyManagementService {

    private final ExchangeClient exchangeClient;
    private final ExchangeMapper exchangeMapper;
    private final CurrencyManagementRepository currencyManagementRepository;

    private static final Logger logger = LogManager.getLogger(CurrencyManagementServiceImpl.class);

    @Value("${currency-layer.api.key}")
    private String apiKey;

    @Cacheable(value = "exchangeRates", key = "T(java.lang.String).valueOf(#exchangeRateRequest.base).toUpperCase() + '-' + T(java.lang.String).valueOf(#exchangeRateRequest.target).toUpperCase()")
    public ExchangeRateResponse getExchangeRate(ExchangeRateRequest exchangeRateRequest) {
        logger.info("Fetching data from API: {}-{}", 
            exchangeRateRequest.getBase(), 
            exchangeRateRequest.getTarget());

        checkNullCurrency(exchangeRateRequest.getBase(), exchangeRateRequest.getTarget(), "Currency symbols can not be null. Please enter valid currency codes: " + Arrays.toString(CurrencySymbol.values()));

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

    @Transactional
    public CurrencyConversionDto currencyConvert(CurrencyConversionRequest currencyConversionRequest) {
        logger.info("Starting currency conversion: {} {} to {}",
                currencyConversionRequest.getAmount(),
                currencyConversionRequest.getBase(),
                currencyConversionRequest.getTarget());

        checkNullCurrency(currencyConversionRequest.getBase(), currencyConversionRequest.getTarget(),
                ("Currency symbols can not be null. Please enter valid currency codes: "+ Arrays.toString(CurrencySymbol.values())));

        String base = currencyConversionRequest.getBase().toUpperCase();
        String target = currencyConversionRequest.getTarget().toUpperCase();

        validateCurrencySymbol(base, "base");
        validateCurrencySymbol(target, "target");

        logger.debug("Making API call to convert currency: {} {} to {}",
                currencyConversionRequest.getAmount(), base, target);
        CurrencyConversionApiResponse currencyConversionApiResponse = exchangeClient.convertCurrency(apiKey, base, target,
                currencyConversionRequest.getAmount());

        CurrencyConverterEntity currencyConverterEntity = exchangeMapper.toCurrencyConverterEntity(currencyConversionApiResponse);
        CurrencyConverterEntity savedCurrencyConverter = currencyManagementRepository.save(currencyConverterEntity);

        logger.info("Currency conversion completed successfully. Transaction ID: {}",
                savedCurrencyConverter.getTransactionId());
        return exchangeMapper.toCurrencyConversionDto(savedCurrencyConverter);
    }

    public CurrencyConverterHistoryPaginationResponse getConversionHistory(CurrencyHistoryRequest currencyHistoryRequest) {
        logger.info("Fetching conversion history. Transaction ID: {}, Date: {}",
                currencyHistoryRequest.getTransactionId().orElse("Not provided"),
                currencyHistoryRequest.getTransactionDate().orElse(null));

        boolean isTransactionIdPresent = currencyHistoryRequest.getTransactionId().isPresent();
        boolean isTransactionDatePresent = currencyHistoryRequest.getTransactionDate().isPresent();

        if (!(isTransactionIdPresent) && !(isTransactionDatePresent)){
            logger.error("No search parameters provided for transaction history");
            throw new TransactionParameterRequiredException("At least one of transactionId or transactionDate parameters is required for transaction history.");
        }

        PageRequest pageRequest = PageRequest.of(currencyHistoryRequest.getPageNumber(), currencyHistoryRequest.getPageSize());
        logger.debug("Page request created - Page: {}, Size: {}",
                currencyHistoryRequest.getPageNumber(),
                currencyHistoryRequest.getPageSize());

        Page<CurrencyConverterEntity> result;

        if (isTransactionDatePresent && isTransactionIdPresent) {
            logger.debug("Searching by both transaction ID and date");
            result = currencyManagementRepository.findByTransactionIdAndTransactionDate(
                    currencyHistoryRequest.getTransactionId().get(),
                    currencyHistoryRequest.getTransactionDate().get(),
                    pageRequest
            );
            checkTransactionHistoryContent(result, "Transaction Id/Date Not Found");
        } else if (isTransactionIdPresent) {
            logger.debug("Searching by transaction ID only");
            result = currencyManagementRepository.findByTransactionId(
                    currencyHistoryRequest.getTransactionId().get(),
                    pageRequest
            );
            checkTransactionHistoryContent(result, "Transaction Id Not Found: " +
                    currencyHistoryRequest.getTransactionId().get());
        } else {
            logger.debug("Searching by transaction date only");
            result = currencyManagementRepository.findByTransactionDate(
                    currencyHistoryRequest.getTransactionDate().get(),
                    pageRequest
            );
            checkTransactionHistoryContent(result, "Transaction Date Not Found: " +
                    currencyHistoryRequest.getTransactionDate().get());
        }

        logger.info("Found {} records in transaction history", result.getTotalElements());
        return exchangeMapper.getCurrencyHistoryPagination(result);
    }

    private static void checkNullCurrency(String currencyConversionRequest, String currencyConversionRequest1, String message) {
        if (Objects.isNull(currencyConversionRequest) ||
                Objects.isNull(currencyConversionRequest1)) {
            logger.error("Currency symbols are null. Base: {}, Target: {}",
                    currencyConversionRequest,
                    currencyConversionRequest1);
            throw new CurrencySymbolNullException(message);
        }
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

    private void checkTransactionHistoryContent(Page<CurrencyConverterEntity> result, String message) {
        if (!result.hasContent()) {
            logger.error("Transaction history not found: {}", message);
            throw new TransactionHistoryNotFoundException(message);
        }
    }

}





