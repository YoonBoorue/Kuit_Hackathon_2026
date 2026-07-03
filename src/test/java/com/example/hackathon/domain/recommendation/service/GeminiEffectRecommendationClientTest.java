package com.example.hackathon.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.example.hackathon.domain.recommendation.config.GeminiRecommendationProperties;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeminiEffectRecommendationClientTest {

    private static final GeminiRecommendationProperties PROPERTIES =
            new GeminiRecommendationProperties("test-api-key", "https://gemini.test", "test-model", 1_000);

    @Test
    void returnsEmptyScoresWhenGeminiReturnsHttpError() {
        RestClient.Builder builder = RestClient.builder().baseUrl(PROPERTIES.getBaseUrl());
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeminiEffectRecommendationClient client = new GeminiEffectRecommendationClient(builder.build(), PROPERTIES);
        server.expect(requestTo(
                        "https://gemini.test/models/test-model:generateContent?key=test-api-key"
                ))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"unavailable\"}"));

        Map<String, Integer> scores = client.recommendScores("title", "description", "situation");

        assertThat(scores).isEmpty();
        server.verify();
    }

    @Test
    void returnsEmptyScoresWhenGeminiRequestFails() {
        RestClient restClient = RestClient.builder()
                .baseUrl(PROPERTIES.getBaseUrl())
                .requestFactory((uri, method) -> {
                    throw new IOException("network unavailable");
                })
                .build();
        GeminiEffectRecommendationClient client = new GeminiEffectRecommendationClient(restClient, PROPERTIES);

        Map<String, Integer> scores = client.recommendScores("title", "description", "situation");

        assertThat(scores).isEmpty();
    }
}
