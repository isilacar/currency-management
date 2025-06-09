package com.openpayd.currency_management.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateRequest {
    @NotBlank(message = "Base currency cannot be null or empty")
    private String base;

    @NotBlank(message = "Target currency cannot be null or empty")
    private String target;
}
