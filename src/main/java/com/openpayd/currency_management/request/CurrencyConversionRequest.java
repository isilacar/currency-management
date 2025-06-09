package com.openpayd.currency_management.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversionRequest {
    @NotBlank(message = "Base currency cannot be null or empty")
    private String base;

    @NotBlank(message = "Target currency cannot be null or empty")
    private String target;

    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
}
