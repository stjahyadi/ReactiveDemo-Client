package com.reactivedemo.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Log4j2
public class WebClientStockClient {

    private WebClient webClient;

    public WebClientStockClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<StockPrice> pricesFor(String symbol) {
        return webClient.get()
                .uri("http://localhost:8080/stocks/{symbol}", symbol)
                .retrieve()
                .bodyToFlux(StockPrice.class)
                .retryWhen(retrySpec())
                .doOnError(IOException.class, e -> log.error(e.getMessage()));
    }

    private RetryBackoffSpec retrySpec() {
        return Retry
                .backoff(5, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(20));
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class StockPrice {
    private String symbol;
    private double price;
    private LocalDateTime time;
}