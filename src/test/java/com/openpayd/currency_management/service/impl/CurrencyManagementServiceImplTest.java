package com.openpayd.currency_management.service.impl;

import com.openpayd.currency_management.client.ExchangeClient;
import com.openpayd.currency_management.client.response.CurrencyConversionApiResponse;
import com.openpayd.currency_management.client.response.ExchangeRateApiResponse;
import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.entity.CurrencyConverterEntity;
import com.openpayd.currency_management.exception.*;
import com.openpayd.currency_management.mapper.ExchangeMapper;
import com.openpayd.currency_management.repository.CurrencyManagementRepository;
import com.openpayd.currency_management.request.CurrencyConversionRequest;
import com.openpayd.currency_management.request.CurrencyHistoryRequest;
import com.openpayd.currency_management.request.ExchangeRateRequest;
import com.openpayd.currency_management.response.CurrencyConversionResponse;
import com.openpayd.currency_management.response.CurrencyConverterHistoryPaginationResponse;
import com.openpayd.currency_management.response.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyManagementServiceImplTest {

    @Mock
    private ExchangeClient exchangeClient;

    @Mock
    private ExchangeMapper exchangeMapper;

    @Mock
    private CurrencyManagementRepository currencyManagementRepository;

    @InjectMocks
    private CurrencyManagementServiceImpl currencyManagementService;

    private static final String API_KEY = "test-api-key";
    private ExchangeRateRequest exchangeRateRequest;
    private CurrencyConversionApiResponse currencyConversionApiResponse;
    private CurrencyConverterEntity currencyConverterEntity;
    private CurrencyConversionRequest currencyConversionRequest;
    private CurrencyConversionDto currencyConversionDto;
    private Page<CurrencyConverterEntity> currencyConverterEntityPage;
    private CurrencyConverterHistoryPaginationResponse currencyConverterHistoryPaginationResponse;
    private CurrencyHistoryRequest currencyHistoryRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(currencyManagementService, "apiKey", API_KEY, String.class);

        exchangeRateRequest = ExchangeRateRequest.builder()
                .base("USD")
                .target("EUR")
                .build();

        currencyConversionRequest = CurrencyConversionRequest.builder()
                .base("USD")
                .target("EUR")
                .amount(100.0)
                .build();

        currencyConversionApiResponse = createDummyCurrencyConversionResponse(
                currencyConversionRequest.getBase(),
                currencyConversionRequest.getTarget(),
                currencyConversionRequest.getAmount()
        );

        currencyConverterEntity = CurrencyConverterEntity.builder()
                .baseCurrency(currencyConversionRequest.getBase().toUpperCase())
                .targetCurrency(currencyConversionRequest.getTarget().toUpperCase())
                .amount(currencyConversionRequest.getAmount())
                .convertedAmount(150.0)
                .exchangeRate(1.5)
                .build();

        currencyConversionDto = CurrencyConversionDto.builder()
                .baseCurrency(currencyConversionRequest.getBase().toUpperCase())
                .targetCurrency(currencyConversionRequest.getTarget().toUpperCase())
                .amount(currencyConversionRequest.getAmount())
                .convertedAmount(150.0)
                .exchangeRate(1.5)
                .build();

        @SuppressWarnings("unchecked")
        Page<CurrencyConverterEntity> page = mock(Page.class);
        currencyConverterEntityPage = page;

        currencyConverterHistoryPaginationResponse = CurrencyConverterHistoryPaginationResponse.builder()
                .currencyHistoryResponseList(List.of())
                .totalValue(1L)
                .totalPages(1L)
                .currentPage(0L)
                .viewedValueCount(3L)
                .build();

        currencyHistoryRequest = CurrencyHistoryRequest.builder()
                .transactionId(Optional.of("transaction-id"))
                .transactionDate(Optional.of(LocalDate.now()))
                .pageNumber(0)
                .pageSize(3)
                .build();

    }

    // exchange rates tests

    @Test
    void getExchangeRate_ShouldReturnExchangeRateResponse() {

        ExchangeRateApiResponse dummyResponse = createDummyExchangeRateResponse("USD", "EUR");
        when(exchangeClient.getExchangeRate(any(), any(), any())).thenReturn(dummyResponse);

        ExchangeRateResponse expectedResponse = ExchangeRateResponse.builder()
                .baseCurrency(exchangeRateRequest.getBase())
                .targetCurrency(exchangeRateRequest.getTarget())
                .exchangeRate(1.5)
                .build();
        when(exchangeMapper.toExchangeRateResponse(any(ExchangeRateApiResponse.class))).thenReturn(expectedResponse);


        ExchangeRateResponse result = currencyManagementService.getExchangeRate(exchangeRateRequest);


        assertNotNull(result);
        assertEquals(exchangeRateRequest.getBase(), result.getBaseCurrency());
        assertEquals(exchangeRateRequest.getTarget(), result.getTargetCurrency());
        assertEquals(1.5, result.getExchangeRate());
        verify(exchangeClient).getExchangeRate(API_KEY, "USD", "EUR");
        verify(exchangeMapper).toExchangeRateResponse(dummyResponse);
    }

    @Test
    void getExchangeRate_WhenBaseCurrencyIsNull_ShouldThrowCurrencySymbolNullException() {

        exchangeRateRequest.setBase(null);

        CurrencySymbolNullException exception = assertThrows(CurrencySymbolNullException.class,
                () -> currencyManagementService.getExchangeRate(exchangeRateRequest));

        assertTrue(exception.getMessage().contains("Currency symbols can not be null"));
        verify(exchangeClient, never()).getExchangeRate(any(), any(), any());
        verify(exchangeMapper, never()).toExchangeRateResponse(any());
    }

    @Test
    void getExchangeRate_WhenTargetCurrencyIsNull_ShouldThrowCurrencySymbolNullException() {

        exchangeRateRequest.setTarget(null);

        CurrencySymbolNullException exception = assertThrows(CurrencySymbolNullException.class,
                () -> currencyManagementService.getExchangeRate(exchangeRateRequest));

        assertTrue(exception.getMessage().contains("Currency symbols can not be null"));
        verify(exchangeClient, never()).getExchangeRate(any(), any(), any());
        verify(exchangeMapper, never()).toExchangeRateResponse(any());
    }

    @Test
    void getExchangeRate_WhenBaseCurrencyIsInvalid_ShouldThrowCurrencySymbolNotFoundException() {

        exchangeRateRequest.setBase("INVALID");

        CurrencySymbolNotFoundException exception = assertThrows(CurrencySymbolNotFoundException.class,
                () -> currencyManagementService.getExchangeRate(exchangeRateRequest));

        assertTrue(exception.getMessage().contains("Invalid base currency symbol"));
        verify(exchangeClient, never()).getExchangeRate(any(), any(), any());
        verify(exchangeMapper, never()).toExchangeRateResponse(any());
    }

    @Test
    void getExchangeRate_WhenTargetCurrencyIsInvalid_ShouldThrowCurrencySymbolNotFoundException() {

        exchangeRateRequest.setTarget("INVALID");

        CurrencySymbolNotFoundException exception = assertThrows(CurrencySymbolNotFoundException.class,
                () -> currencyManagementService.getExchangeRate(exchangeRateRequest));

        assertTrue(exception.getMessage().contains("Invalid target currency symbol"));
        verify(exchangeClient, never()).getExchangeRate(any(), any(), any());
        verify(exchangeMapper, never()).toExchangeRateResponse(any());
    }

    @Test
    void currencyConvert_ShouldReturnCurrencyConversionDto() {

        when(exchangeClient.convertCurrency(
                anyString(),
                anyString(),
                anyString(),
                anyDouble()
        )).thenReturn(currencyConversionApiResponse);

        when(exchangeMapper.toCurrencyConverterEntity(any(CurrencyConversionApiResponse.class))).thenReturn(currencyConverterEntity);
        when(currencyManagementRepository.save(any(CurrencyConverterEntity.class))).thenReturn(currencyConverterEntity);
        when(exchangeMapper.toCurrencyConversionDto(any(CurrencyConverterEntity.class))).thenReturn(currencyConversionDto);

        CurrencyConversionDto result = currencyManagementService.currencyConvert(currencyConversionRequest);

        assertNotNull(result);
        assertEquals(currencyConversionRequest.getBase().toUpperCase(), result.getBaseCurrency());
        assertEquals(currencyConversionRequest.getTarget().toUpperCase(), result.getTargetCurrency());
        assertEquals(currencyConversionRequest.getAmount(), result.getAmount());
        assertEquals(150.0, result.getConvertedAmount());
        assertEquals(1.5, result.getExchangeRate());

        verify(exchangeClient).convertCurrency(
                API_KEY,
                currencyConversionRequest.getBase().toUpperCase(),
                currencyConversionRequest.getTarget().toUpperCase(),
                currencyConversionRequest.getAmount()
        );
        verify(exchangeMapper).toCurrencyConverterEntity(currencyConversionApiResponse);
        verify(currencyManagementRepository).save(currencyConverterEntity);
        verify(exchangeMapper).toCurrencyConversionDto(currencyConverterEntity);
    }

    @Test
    void currencyConvert_WhenBaseCurrencyIsNull_ShouldThrowCurrencySymbolNullException() {

        currencyConversionRequest.setBase(null);


        CurrencySymbolNullException exception = assertThrows(CurrencySymbolNullException.class,
                () -> currencyManagementService.currencyConvert(currencyConversionRequest));

        assertTrue(exception.getMessage().contains("Currency symbols can not be null"));
        verify(exchangeClient, never()).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, never()).toCurrencyConverterEntity(any());
        verify(currencyManagementRepository, never()).save(any());
    }

    @Test
    void currencyConvert_WhenTargetCurrencyIsNull_ShouldThrowCurrencySymbolNullException() {

        currencyConversionRequest.setTarget(null);


        CurrencySymbolNullException exception = assertThrows(CurrencySymbolNullException.class,
                () -> currencyManagementService.currencyConvert(currencyConversionRequest));

        assertTrue(exception.getMessage().contains("Currency symbols can not be null"));
        verify(exchangeClient, never()).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, never()).toCurrencyConverterEntity(any());
        verify(currencyManagementRepository, never()).save(any());
    }

    @Test
    void currencyConvert_WhenBaseCurrencyIsInvalid_ShouldThrowCurrencySymbolNotFoundException() {

        currencyConversionRequest.setBase("INVALID");


        CurrencySymbolNotFoundException exception = assertThrows(CurrencySymbolNotFoundException.class,
                () -> currencyManagementService.currencyConvert(currencyConversionRequest));

        assertTrue(exception.getMessage().contains("Invalid base currency symbol"));
        verify(exchangeClient, never()).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, never()).toCurrencyConverterEntity(any());
        verify(currencyManagementRepository, never()).save(any());
    }

    @Test
    void currencyConvert_WhenTargetCurrencyIsInvalid_ShouldThrowCurrencySymbolNotFoundException() {

        currencyConversionRequest.setTarget("INVALID");


        CurrencySymbolNotFoundException exception = assertThrows(CurrencySymbolNotFoundException.class,
                () -> currencyManagementService.currencyConvert(currencyConversionRequest));

        assertTrue(exception.getMessage().contains("Invalid target currency symbol"));
        verify(exchangeClient, never()).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, never()).toCurrencyConverterEntity(any());
        verify(currencyManagementRepository, never()).save(any());
    }

    @Test
    void currencyHistory_WhenTransactionIdAndDateIsNotNullAndValid_ShouldReturnCurrencyHistoryResponse() {

        when(currencyManagementRepository.findByTransactionIdAndTransactionDate(
                anyString(),
                any(LocalDate.class),
                any(PageRequest.class)
        )).thenReturn(currencyConverterEntityPage);

        when(currencyConverterEntityPage.hasContent()).thenReturn(true);
        when(exchangeMapper.getCurrencyHistoryPagination(any())).thenReturn(currencyConverterHistoryPaginationResponse);

        CurrencyConverterHistoryPaginationResponse result = currencyManagementService.getConversionHistory(currencyHistoryRequest);

        assertNotNull(result);
        assertNotNull(result.getCurrencyHistoryResponseList());
        assertEquals(1L, result.getTotalValue());
        assertEquals(1L, result.getTotalPages());
        assertEquals(0L, result.getCurrentPage());
        assertEquals(3L, result.getViewedValueCount());

        verify(currencyManagementRepository).findByTransactionIdAndTransactionDate(
                currencyHistoryRequest.getTransactionId().get(),
                currencyHistoryRequest.getTransactionDate().get(),
                PageRequest.of(currencyHistoryRequest.getPageNumber(), currencyHistoryRequest.getPageSize())
        );
        verify(exchangeMapper).getCurrencyHistoryPagination(currencyConverterEntityPage);
    }

    @Test
    void currencyHistory_WhenOnlyTransactionIdIsPresent_ShouldReturnCurrencyHistoryResponse() {

        currencyHistoryRequest.setTransactionDate(Optional.empty());

        when(currencyManagementRepository.findByTransactionId(
                anyString(),
                any(PageRequest.class)
        )).thenReturn(currencyConverterEntityPage);

        when(currencyConverterEntityPage.hasContent()).thenReturn(true);

        when(exchangeMapper.getCurrencyHistoryPagination(any())).thenReturn(currencyConverterHistoryPaginationResponse);

        CurrencyConverterHistoryPaginationResponse result = currencyManagementService.getConversionHistory(currencyHistoryRequest);

        assertNotNull(result);
        assertNotNull(result.getCurrencyHistoryResponseList());
        assertEquals(1L, result.getTotalValue());
        assertEquals(1L, result.getTotalPages());
        assertEquals(0L, result.getCurrentPage());
        assertEquals(3L, result.getViewedValueCount());

        verify(currencyManagementRepository).findByTransactionId(
                currencyHistoryRequest.getTransactionId().get(),
                PageRequest.of(currencyHistoryRequest.getPageNumber(), currencyHistoryRequest.getPageSize())
        );
        verify(exchangeMapper).getCurrencyHistoryPagination(currencyConverterEntityPage);
    }

    @Test
    void currencyHistory_WhenOnlyTransactionDateIsPresent_ShouldReturnCurrencyHistoryResponse() {

        currencyHistoryRequest.setTransactionId(Optional.empty());

        when(currencyManagementRepository.findByTransactionDate(
                any(LocalDate.class),
                any(PageRequest.class)
        )).thenReturn(currencyConverterEntityPage);

        when(currencyConverterEntityPage.hasContent()).thenReturn(true);

        when(exchangeMapper.getCurrencyHistoryPagination(any())).thenReturn(currencyConverterHistoryPaginationResponse);

        CurrencyConverterHistoryPaginationResponse result = currencyManagementService.getConversionHistory(currencyHistoryRequest);


        assertNotNull(result);
        assertNotNull(result.getCurrencyHistoryResponseList());
        assertEquals(1L, result.getTotalValue());
        assertEquals(1L, result.getTotalPages());
        assertEquals(0L, result.getCurrentPage());
        assertEquals(3L, result.getViewedValueCount());

        verify(currencyManagementRepository).findByTransactionDate(
                currencyHistoryRequest.getTransactionDate().get(),
                PageRequest.of(currencyHistoryRequest.getPageNumber(), currencyHistoryRequest.getPageSize())
        );
        verify(exchangeMapper).getCurrencyHistoryPagination(currencyConverterEntityPage);
    }

    @Test
    void currencyHistory_WhenBothTransactionIdAndDateAreNull_ShouldThrowTransactionParameterRequiredException() {

        currencyHistoryRequest = CurrencyHistoryRequest.builder()
                .transactionId(Optional.empty())
                .transactionDate(Optional.empty())
                .pageNumber(0)
                .pageSize(3)
                .build();


        TransactionParameterRequiredException exception = assertThrows(
                TransactionParameterRequiredException.class,
                () -> currencyManagementService.getConversionHistory(currencyHistoryRequest)
        );

        assertTrue(exception.getMessage().contains("At least one of transactionId or transactionDate parameters is required"));
        verify(currencyManagementRepository, never()).findByTransactionIdAndTransactionDate(any(), any(), any());
        verify(currencyManagementRepository, never()).findByTransactionId(any(), any());
        verify(currencyManagementRepository, never()).findByTransactionDate(any(), any());
        verify(exchangeMapper, never()).getCurrencyHistoryPagination(any());
    }

    @Test
    void currencyHistory_WhenNoContentFound_ShouldThrowTransactionHistoryNotFoundException() {

        when(currencyManagementRepository.findByTransactionIdAndTransactionDate(
                anyString(),
                any(LocalDate.class),
                any(PageRequest.class)
        )).thenReturn(currencyConverterEntityPage);

        when(currencyConverterEntityPage.hasContent()).thenReturn(false);


        TransactionHistoryNotFoundException exception = assertThrows(
                TransactionHistoryNotFoundException.class,
                () -> currencyManagementService.getConversionHistory(currencyHistoryRequest)
        );

        assertTrue(exception.getMessage().contains("Transaction Id/Date Not Found"));
        verify(currencyManagementRepository).findByTransactionIdAndTransactionDate(
                currencyHistoryRequest.getTransactionId().get(),
                currencyHistoryRequest.getTransactionDate().get(),
                PageRequest.of(currencyHistoryRequest.getPageNumber(), currencyHistoryRequest.getPageSize())
        );
        verify(exchangeMapper, never()).getCurrencyHistoryPagination(any());
    }

    @Test
    void processBulkConversion_WhenValidFile_ShouldReturnConversionResponses(){
        String csvContent = "base,target,amount\nUSD,EUR,100\nEUR,TRY,200";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "conversions.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent.getBytes()
        );

        CurrencyConversionApiResponse response1 = createDummyCurrencyConversionResponse("USD", "EUR", 100.0);
        CurrencyConversionApiResponse response2 = createDummyCurrencyConversionResponse("EUR", "TRY", 200.0);

        when(exchangeClient.convertCurrency(anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(response1);
        when(exchangeClient.convertCurrency(anyString(), anyString(), anyString(), anyDouble()))
                .thenReturn(response2);

        CurrencyConverterEntity entity1 = getEntity(response1);

        CurrencyConverterEntity entity2=getEntity(response2);

        when(exchangeMapper.convertCurrency(any(CurrencyConversionRequest.class), any(CurrencyConversionApiResponse.class)))
                .thenReturn(entity1, entity2);

        when(currencyManagementRepository.save(any(CurrencyConverterEntity.class))).thenReturn(entity1, entity2);

        CurrencyConversionResponse responseDto1 = getResponseDto(entity1);

        CurrencyConversionResponse responseDto2 = getResponseDto(entity2);

        when(exchangeMapper.toCurrencyConversionResponse(any(CurrencyConverterEntity.class))).thenReturn(responseDto1, responseDto2);


        List<CurrencyConversionResponse> result = currencyManagementService.processBulkConversions(file);


        assertNotNull(result);
        assertEquals(2, result.size());

        CurrencyConversionResponse firstResponse = result.get(0);
        assertEquals("USD", firstResponse.getBaseCurrency());
        assertEquals("EUR", firstResponse.getTargetCurrency());
        assertEquals(100.0, firstResponse.getAmount());
        assertEquals(150.0, firstResponse.getConvertedAmount());
        assertEquals(1.5, firstResponse.getExchangeRate());

        CurrencyConversionResponse secondResponse = result.get(1);
        assertEquals("EUR", secondResponse.getBaseCurrency());
        assertEquals("TRY", secondResponse.getTargetCurrency());
        assertEquals(200.0, secondResponse.getAmount());
        assertEquals(300.0, secondResponse.getConvertedAmount());
        assertEquals(1.5, secondResponse.getExchangeRate());

        verify(exchangeClient, times(2)).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, times(2)).convertCurrency(any(), any());
        verify(currencyManagementRepository, times(2)).save(any());
        verify(exchangeMapper, times(2)).toCurrencyConversionResponse(any(CurrencyConverterEntity.class));
    }

    @Test
    void processBulkConversion_WhenInvalidCurrencySymbol_ShouldThrowCurrencySymbolNotFoundException() throws IOException {

        String csvContent = "base,target,amount\nINVALID,EUR,100";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "conversions.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent.getBytes()
        );


        CurrencySymbolNotFoundException exception = assertThrows(
                CurrencySymbolNotFoundException.class,
                () -> currencyManagementService.processBulkConversions(file)
        );

        assertTrue(exception.getMessage().contains("Invalid base currency symbol"));
        verify(exchangeClient, never()).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, never()).convertCurrency(any(), any());
        verify(currencyManagementRepository, never()).save(any());
    }

    @Test
    void processBulkConversion_WhenInvalidAmount_ShouldThrowBulkCurrencyConversionException() throws IOException {

        String csvContent = "base,target,amount\nUSD,EUR,-100";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "conversions.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csvContent.getBytes()
        );


        BulkCurrencyConversionException exception = assertThrows(
                BulkCurrencyConversionException.class,
                () -> currencyManagementService.processBulkConversions(file)
        );

        assertTrue(exception.getMessage().contains("Invalid amount"));
        verify(exchangeClient, never()).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, never()).convertCurrency(any(), any());
        verify(currencyManagementRepository, never()).save(any());
    }

    @Test
    void processBulkConversion_WhenFileIsEmpty_ShouldThrowFileUploadException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.csv",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        FileUploadException exception = assertThrows(FileUploadException.class,
                () -> currencyManagementService.processBulkConversions(emptyFile));

        assertEquals("Please upload a valid CSV file.", exception.getMessage());
    }

    @Test
    void processBulkConversion_WhenFileReadError_ShouldThrowFileUploadException() throws IOException {
        MockMultipartFile mockFile = mock(MockMultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.csv");
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getInputStream()).thenThrow(new IOException("Test error"));

        FileUploadException exception = assertThrows(
                FileUploadException.class,
                () -> currencyManagementService.processBulkConversions(mockFile)
        );

        assertTrue(exception.getMessage().contains("Error processing the uploaded file"));
        verify(exchangeClient, never()).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, never()).convertCurrency(any(), any());
        verify(currencyManagementRepository, never()).save(any());
    }

    @Test
    void processBulkConversion_WhenFileExtensionIsNotCsv_ShouldThrowFileUploadException() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        FileUploadException exception = assertThrows(FileUploadException.class,
                () -> currencyManagementService.processBulkConversions(invalidFile));

        assertEquals("Only CSV files are allowed. Please upload a file with .csv extension.", exception.getMessage());
    }

    @Test
    void processBulkConversion_WhenFileNameIsNull_ShouldThrowFileUploadException() {
        MockMultipartFile nullFileNameFile = new MockMultipartFile(
                "file",
                null,
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        FileUploadException exception = assertThrows(FileUploadException.class,
                () -> currencyManagementService.processBulkConversions(nullFileNameFile));

        assertEquals("Only CSV files are allowed. Please upload a file with .csv extension.", exception.getMessage());
    }

    @Test
    void processBulkConversion_WhenFileIsNull_ShouldThrowFileUploadException() {
        MockMultipartFile nullFile = null;

        FileUploadException exception = assertThrows(FileUploadException.class,
                () -> currencyManagementService.processBulkConversions(nullFile));

        assertEquals("Please upload a valid CSV file.", exception.getMessage());
        verify(exchangeClient, never()).convertCurrency(anyString(), anyString(), anyString(), anyDouble());
        verify(exchangeMapper, never()).convertCurrency(any(), any());
        verify(currencyManagementRepository, never()).save(any());
    }

    private ExchangeRateApiResponse createDummyExchangeRateResponse(String base, String target) {
        Map<String, Double> quotes = new HashMap<>();
        quotes.put(base + target, 1.5);
        return new ExchangeRateApiResponse(true, base, quotes);
    }

    private CurrencyConversionApiResponse createDummyCurrencyConversionResponse(String base, String target, double amount) {
        CurrencyConversionApiResponse response = new CurrencyConversionApiResponse();
        CurrencyConversionApiResponse.Query query = new CurrencyConversionApiResponse.Query();
        query.setFrom(base);
        query.setTo(target);
        query.setAmount(amount);
        response.setQuery(query);

        CurrencyConversionApiResponse.Info info = new CurrencyConversionApiResponse.Info();
        info.setQuote(1.5);

        response.setInfo(info);
        response.setResult(amount * 1.5);

        return response;
    }

    private CurrencyConversionResponse getResponseDto(CurrencyConverterEntity entity) {
        CurrencyConversionResponse responseDto = CurrencyConversionResponse.builder()
                .baseCurrency(entity.getBaseCurrency())
                .targetCurrency(entity.getTargetCurrency())
                .amount(entity.getAmount())
                .convertedAmount(entity.getConvertedAmount())
                .exchangeRate(entity.getExchangeRate())
                .build();
        return responseDto;
    }

    private CurrencyConverterEntity getEntity(CurrencyConversionApiResponse response) {
        CurrencyConverterEntity entity = CurrencyConverterEntity.builder()
                .baseCurrency(response.getQuery().getFrom())
                .targetCurrency(response.getQuery().getTo())
                .amount(response.getQuery().getAmount())
                .convertedAmount(response.getResult())
                .exchangeRate(response.getInfo().getQuote())
                .build();
        return entity;
    }

}
