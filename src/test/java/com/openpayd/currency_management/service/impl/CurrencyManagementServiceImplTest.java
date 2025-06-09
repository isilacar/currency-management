package com.openpayd.currency_management.service.impl;

import com.openpayd.currency_management.client.ExchangeClient;
import com.openpayd.currency_management.client.response.CurrencyConversionApiResponse;
import com.openpayd.currency_management.client.response.ExchangeRateApiResponse;
import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.entity.CurrencyConverterEntity;
import com.openpayd.currency_management.exception.CurrencySymbolNotFoundException;
import com.openpayd.currency_management.exception.CurrencySymbolNullException;
import com.openpayd.currency_management.mapper.ExchangeMapper;
import com.openpayd.currency_management.repository.CurrencyManagementRepository;
import com.openpayd.currency_management.request.CurrencyConversionRequest;
import com.openpayd.currency_management.request.ExchangeRateRequest;
import com.openpayd.currency_management.response.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

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

}
