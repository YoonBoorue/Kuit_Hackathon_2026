package com.example.hackathon.domain.recommendation.config;

public class GeminiRecommendationProperties {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final long timeoutMs;

    public GeminiRecommendationProperties(String apiKey, String baseUrl, String model, long timeoutMs) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = requireText(baseUrl, "Gemini baseUrl 설정이 필요합니다.");
        this.model = normalizeModel(requireText(model, "Gemini model 설정이 필요합니다."));
        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("Gemini timeout-ms 설정은 0보다 커야 합니다.");
        }
        this.timeoutMs = timeoutMs;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public long getTimeoutMs() {
        return timeoutMs;
    }

    public boolean hasApiKey() {
        return !apiKey.isBlank();
    }

    private String normalizeModel(String model) {
        if (model.startsWith("models/")) {
            return model.substring("models/".length());
        }
        return model;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
