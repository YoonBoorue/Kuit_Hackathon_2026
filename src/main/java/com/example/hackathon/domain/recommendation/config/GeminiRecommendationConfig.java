package com.example.hackathon.domain.recommendation.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiRecommendationConfig {

    @Bean
    public GeminiRecommendationProperties geminiRecommendationProperties(
            @Value("${app.ai.gemini.api-key:}") String apiKey,
            @Value("${app.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${app.ai.gemini.model:gemini-3.5-flash}") String model,
            @Value("${app.ai.gemini.timeout-ms:10000}") long timeoutMs
    ) {
        return new GeminiRecommendationProperties(apiKey, baseUrl, model, timeoutMs);
    }

    @Bean
    public RestClient geminiRestClient(GeminiRecommendationProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        Duration timeout = Duration.ofMillis(properties.getTimeoutMs());
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
