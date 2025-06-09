package com.openpayd.currency_management.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
    public class CurrencyConverterHistoryPaginationResponse {
    private List<CurrencyHistoryResponse> currencyHistoryResponseList;
    private long totalValue;
    private long totalPages;
    private long currentPage;
    private long viewedValueCount;
}
