package com.example.hackathon.domain.recommendation.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class EffectRecommendationDtos {

    private EffectRecommendationDtos() {
    }

    public record RecommendEffectsRequest(
            @NotBlank(message = "생존법 제목은 필수입니다.")
            @Size(max = 20, message = "생존법 제목은 20자 이하여야 합니다.")
            String title,

            @NotBlank(message = "생존법 설명은 필수입니다.")
            @Size(max = 100, message = "생존법 설명은 100자 이하여야 합니다.")
            String description
    ) {
    }

    public record RecommendEffectsResponse(
            Long primaryEffectTypeId,
            List<RecommendedEffectResponse> effects,
            List<EffectScoreResponse> scores
    ) {
    }

    public record RecommendedEffectResponse(
            Long effectTypeId,
            String code,
            String name,
            String icon,
            String color,
            Short level,
            Short displayOrder
    ) {
    }

    public record EffectScoreResponse(
            Long effectTypeId,
            String code,
            String name,
            Short score
    ) {
    }
}
