package com.openpayd.currency_management.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrencyHistoryRequest {
    @NotNull(message = "Transaction ID cannot be null")
    private Optional<String> transactionId;

    @NotNull(message = "Transaction date cannot be null")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Optional<LocalDate> transactionDate;

    @NotNull(message = "Page number cannot be null")
    @Min(value = 0, message = "Page number must be greater than or equal to 0")
    private int pageNumber;

    @NotNull(message = "Page size cannot be null")
    @Min(value = 1, message = "Page size must be greater than 0")
    private int pageSize;
}
