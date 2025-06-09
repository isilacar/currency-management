package com.openpayd.currency_management.client.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyConversionApiResponse {
    private boolean success;
    private Info info;
    private Query query;
    private double result;

    @Getter
    @Setter
    public static class Info {
        private long timestamp;
        private double quote;
    }

    @Getter
    @Setter
    public static class Query{
        private String from;
        private String to;
        private Double amount;
    }
}
