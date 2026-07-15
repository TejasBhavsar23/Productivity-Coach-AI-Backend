package com.productivitycoach.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configures the reactive {@link WebClient} used to call the Claude AI API.
 * Uses Reactor Netty with explicit connect/read/write timeouts to avoid
 * hanging requests in production.
 */
@Configuration
public class WebClientConfig {

    @Value("${webclient.connect-timeout-ms:10000}")
    private int connectTimeoutMs;

    @Value("${webclient.read-timeout-ms:60000}")
    private int readTimeoutMs;

    @Value("${webclient.write-timeout-ms:10000}")
    private int writeTimeoutMs;

    /**
     * General-purpose WebClient bean.
     * Each AI service method sets base URL and headers per request.
     */
    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofMillis(readTimeoutMs))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutMs, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeoutMs, TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
