package com.openpayd.currency_management.controller;


import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.request.CurrencyConversionRequest;
import com.openpayd.currency_management.request.ExchangeRateRequest;
import com.openpayd.currency_management.response.ExchangeRateResponse;
import com.openpayd.currency_management.service.CurrencyManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Currency Management API",
    description = "APIs for currency exchange rate queries, conversions, bulk conversions with CSV file and history management"
)
@RestController
@RequestMapping("/api/v1/currency")
@RequiredArgsConstructor
public class CurrencyManagementController {

    private final CurrencyManagementService currencyManagementService;

    @Operation(
        summary = "Get Exchange Rate",
        description = "Retrieves the current exchange rate between two currencies"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "400", description = "Invalid currency symbols"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/exchange-rate")
    public ResponseEntity<ExchangeRateResponse> getExchangeRate(
        @Valid @RequestBody ExchangeRateRequest exchangeRateRequest
    ) {
        return ResponseEntity.ok(currencyManagementService.getExchangeRate(exchangeRateRequest));
    }


    @Operation(
            summary = "Convert Currency",
            description = "Converts an amount from one currency to another using current exchange rates"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/convert")
    public ResponseEntity<CurrencyConversionDto> currencyConvert(
            @Valid @RequestBody CurrencyConversionRequest currencyConversionRequest
    ) {
        return ResponseEntity.ok(currencyManagementService.currencyConvert(currencyConversionRequest));
    }

} 