package com.openpayd.currency_management.service.impl;

import com.opencsv.bean.CsvToBeanBuilder;
import com.openpayd.currency_management.client.ExchangeClient;
import com.openpayd.currency_management.client.response.CurrencyConversionApiResponse;
import com.openpayd.currency_management.client.response.ExchangeRateApiResponse;
import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.entity.CurrencyConverterEntity;
import com.openpayd.currency_management.enums.CurrencySymbol;
import com.openpayd.currency_management.exception.*;
import com.openpayd.currency_management.mapper.ExchangeMapper;
import com.openpayd.currency_management.repository.CurrencyManagementRepository;
import com.openpayd.currency_management.request.CurrencyConversionRequest;
import com.openpayd.currency_management.request.CurrencyHistoryRequest;
import com.openpayd.currency_management.request.ExchangeRateRequest;
import com.openpayd.currency_management.response.CurrencyConversionResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Transactional
    public List<CurrencyConversionResponse> processBulkConversions(MultipartFile file) {
        checkingFileFormat(file);

        logger.info("Starting bulk conversion process for file: {}", file.getOriginalFilename());

        List<CurrencyConversionRequest> requests = getRequests(file);

        logger.info("Successfully parsed {} conversion requests from CSV", requests.size());

        List<CurrencyConversionResponse> responses = new ArrayList<>();

        for (CurrencyConversionRequest request : requests) {
            try {
                logger.info("Processing: {}/{} - Amount: {}",
                        request.getBase(), request.getTarget(), request.getAmount());

                checkingRequestNullValues(request);

                validateCurrencySymbol(request.getBase(), "base");
                validateCurrencySymbol(request.getTarget(), "target");

                checkingAmountValue(request);

                logger.debug("Making API call for bulk conversion: {} {} to {}",
                        request.getAmount(), request.getBase(), request.getTarget());

                CurrencyConversionApiResponse response = exchangeClient.convertCurrency(
                        apiKey,
                        request.getBase(),
                        request.getTarget(),
                        request.getAmount()
                );

                if (response == null || !response.isSuccess() || response.getResult() == 0 ||
                        response.getInfo() == null || response.getInfo().getQuote() == 0) {
                    logger.error("Invalid API response for conversion: {}/{} - Amount: {}",
                            request.getBase(), request.getTarget(), request.getAmount());
                    continue;
                }

                CurrencyConverterEntity entity = exchangeMapper.convertCurrency(request, response);
                CurrencyConverterEntity savedEntity = currencyManagementRepository.save(entity);
                responses.add(exchangeMapper.toCurrencyConversionResponse(savedEntity));
                logger.info("Success: {}/{} - Amount: {}",
                        request.getBase(), request.getTarget(), request.getAmount());

                // Adding delay between API calls to handle rate limiting and ensure response completeness
                // The external API typically takes 500ms to respond, so we wait 600ms between calls
                Thread.sleep(600);
            } catch (InterruptedException interruptedException){
                logger.error("Error processing conversion: {}/{} - Amount: {}",
                        request.getBase(), request.getTarget(), request.getAmount(), interruptedException);
            }
        }

        checkResponseContent(responses);

        logger.info("Bulk conversion completed. Processed {} requests successfully", responses.size());
        return responses;
    }

    private static void checkResponseContent(List<CurrencyConversionResponse> responses) {
        if (responses.isEmpty()) {
            throw new BulkCurrencyConversionException(
                    "No successful conversions found",
                    null, null, null,
                    "NO_SUCCESSFUL_CONVERSIONS"
            );
        }
    }

    private static void checkingAmountValue(CurrencyConversionRequest request) {
        if (request.getAmount() <= 0) {
            logger.error("Invalid amount in bulk conversion: {} for {}/{}",
                    request.getAmount(), request.getBase(), request.getTarget());

            throw BulkCurrencyConversionException.invalidAmount(
                    request.getBase(),
                    request.getTarget(),
                    request.getAmount()
            );
        }
    }

    private static void checkingRequestNullValues(CurrencyConversionRequest request) {
        if (Objects.isNull(request.getBase()) || Objects.isNull(request.getTarget())) {
            logger.error("Null values found in bulk conversion request. Base: {}, Target: {}",
                    request.getBase(), request.getTarget());
            throw BulkCurrencyConversionException.invalidRequest(
                    request.getBase(),
                    request.getTarget(),
                    request.getAmount()
            );
        }
    }

    private List<CurrencyConversionRequest> getRequests(MultipartFile file) {
        try {
            logger.debug("Starting to parse CSV file: {}", file.getOriginalFilename());
            List<CurrencyConversionRequest> requests = new CsvToBeanBuilder<CurrencyConversionRequest>(
                    new InputStreamReader(file.getInputStream()))
                    .withType(CurrencyConversionRequest.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
            logger.debug("Successfully parsed CSV file");
            return requests;
        } catch (IOException e) {
            logger.error("Error reading CSV file: {}", e.getMessage());
            throw new FileUploadException("Error processing the uploaded file. Please ensure it is a valid CSV file.");
        }
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
    private void checkingFileFormat(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            logger.error("Uploaded file is null or empty");
            throw new FileUploadException("Please upload a valid CSV file.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            logger.error("Invalid file format: {}", fileName);
            throw new FileUploadException("Only CSV files are allowed. Please upload a file with .csv extension.");
        }
    }

}





