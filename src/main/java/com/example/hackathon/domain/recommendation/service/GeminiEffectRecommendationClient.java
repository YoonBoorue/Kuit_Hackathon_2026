package com.example.hackathon.domain.recommendation.service;

import com.example.hackathon.domain.recommendation.config.GeminiRecommendationProperties;
import com.example.hackathon.global.exception.ExternalServiceException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GeminiEffectRecommendationClient {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[^{}]*}");
    private static final Pattern CODE_PATTERN = Pattern.compile("\"code\"\\s*:\\s*\"([A-Z_]+)\"");
    private static final Pattern SCORE_PATTERN = Pattern.compile("\"(?:score|level)\"\\s*:\\s*(\\d+)");
    private static final Logger log = LoggerFactory.getLogger(GeminiEffectRecommendationClient.class);
    private static final int MAX_ERROR_BODY_LOG_LENGTH = 2_000;

    private static final String SYSTEM_PROMPT = """
            You are a classifier that recommends effect scores for a summer survival tip card.
            Return JSON only.
            Allowed codes are exactly COOLING, MENTAL, STAMINA, MONEY, PATIENCE.
            Each code must have an integer score from 1 to 5.
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Recommend scores for all 5 effect categories from the survival tip title, description, and recommended situation.

            Categories:
            - COOLING: relief from heat, cooling, lowering body temperature
            - MENTAL: mood recovery, stress relief, emotional refresh
            - STAMINA: saving energy, recovering from fatigue, physical care
            - MONEY: saving money, cost efficiency, low-cost tips
            - PATIENCE: endurance, staying consistent, making summer bearable

            Return format:
            {"scores":[{"code":"COOLING","score":1},{"code":"MENTAL","score":1},{"code":"STAMINA","score":1},{"code":"MONEY","score":1},{"code":"PATIENCE","score":1}]}

            Title: %s
            Description: %s
            Recommended situation: %s
            """;

    private final RestClient geminiRestClient;
    private final GeminiRecommendationProperties properties;

    public GeminiEffectRecommendationClient(
            RestClient geminiRestClient,
            GeminiRecommendationProperties properties
    ) {
        this.geminiRestClient = geminiRestClient;
        this.properties = properties;
    }

    public Map<String, Integer> recommendScores(String title, String description, String recommendedSituation) {
        if (!properties.hasApiKey()) {
            throw new ExternalServiceException("Gemini API key가 설정되지 않았습니다.");
        }

        try {
            Map<String, Object> response = geminiRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", properties.getApiKey())
                            .build(properties.getModel()))
                    .body(buildRequest(title, description, recommendedSituation))
                    .retrieve()
                    .body(MAP_TYPE);
            return parseScores(extractText(response));
        } catch (RestClientResponseException exception) {
            log.warn(
                    "Gemini API returned an error response. status={}, body={}",
                    exception.getStatusCode(),
                    truncateErrorBody(exception.getResponseBodyAsString())
            );
            throw new ExternalServiceException("AI 효과 추천 서비스를 사용할 수 없습니다.");
        } catch (RestClientException exception) {
            log.warn("Gemini API request failed before receiving a response.", exception);
            throw new ExternalServiceException("AI 효과 추천 서비스를 사용할 수 없습니다.");
        }
    }

    private String truncateErrorBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        if (body.length() <= MAX_ERROR_BODY_LOG_LENGTH) {
            return body;
        }
        return body.substring(0, MAX_ERROR_BODY_LOG_LENGTH) + "...";
    }

    private Map<String, Object> buildRequest(String title, String description, String recommendedSituation) {
        return Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", buildPrompt(title, description, recommendedSituation))))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json")
        );
    }

    private String buildPrompt(String title, String description, String recommendedSituation) {
        return USER_PROMPT_TEMPLATE.formatted(title, description, recommendedSituation);
    }

    private String extractText(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        Object candidatesObject = response.get("candidates");
        if (!(candidatesObject instanceof List<?> candidates) || candidates.isEmpty()) {
            return "";
        }
        Object firstCandidate = candidates.getFirst();
        if (!(firstCandidate instanceof Map<?, ?> candidate)) {
            return "";
        }
        Object contentObject = candidate.get("content");
        if (!(contentObject instanceof Map<?, ?> content)) {
            return "";
        }
        Object partsObject = content.get("parts");
        if (!(partsObject instanceof List<?> parts) || parts.isEmpty()) {
            return "";
        }
        Object firstPart = parts.getFirst();
        if (!(firstPart instanceof Map<?, ?> part)) {
            return "";
        }
        Object text = part.get("text");
        return text instanceof String value ? value : "";
    }

    private Map<String, Integer> parseScores(String text) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        Matcher objectMatcher = JSON_OBJECT_PATTERN.matcher(text == null ? "" : text);
        while (objectMatcher.find()) {
            String object = objectMatcher.group();
            Matcher codeMatcher = CODE_PATTERN.matcher(object);
            Matcher scoreMatcher = SCORE_PATTERN.matcher(object);
            if (codeMatcher.find() && scoreMatcher.find()) {
                scores.put(codeMatcher.group(1), Integer.parseInt(scoreMatcher.group(1)));
            }
        }
        return scores;
    }
}
