package com.openpayd.currency_management.controller;


import com.openpayd.currency_management.dto.CurrencyConversionDto;
import com.openpayd.currency_management.request.CurrencyConversionRequest;
import com.openpayd.currency_management.request.CurrencyHistoryRequest;
import com.openpayd.currency_management.request.ExchangeRateRequest;
import com.openpayd.currency_management.response.CurrencyConversionResponse;
import com.openpayd.currency_management.response.CurrencyConverterHistoryPaginationResponse;
import com.openpayd.currency_management.response.ExchangeRateResponse;
import com.openpayd.currency_management.service.CurrencyManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @Operation(
            summary = "Get Conversion History",
            description = "Retrieves the history of currency conversions with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "404", description = "History not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/history")
    public ResponseEntity<CurrencyConverterHistoryPaginationResponse> getConversionHistory(
            @Valid @RequestBody CurrencyHistoryRequest currencyHistoryRequest
    ) {
        return ResponseEntity.ok(currencyManagementService.getConversionHistory(currencyHistoryRequest));
    }

    @Operation(
            summary = "Bulk Currency Conversion",
            description = "Processes multiple currency conversions from a CSV file"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "400", description = "Invalid file or parameters"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/bulk-convert")
    public ResponseEntity<List<CurrencyConversionResponse>> processBulkConversions(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(currencyManagementService.processBulkConversions(file));
    }

} 