package com.openpayd.currency_management.client;

import com.openpayd.currency_management.client.response.ExchangeRateApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "exchange-api", url = "${currency-layer.api.url}")
public interface ExchangeClient {

    @GetMapping("/live")
    ExchangeRateApiResponse getExchangeRate(
        @RequestParam("access_key") String accessKey,
        @RequestParam("source") String source,
        @RequestParam("currencies") String currencies
    );

}