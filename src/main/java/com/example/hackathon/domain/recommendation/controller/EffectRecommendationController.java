package com.example.hackathon.domain.recommendation.controller;

import com.example.hackathon.domain.recommendation.dto.EffectRecommendationDtos.RecommendEffectsRequest;
import com.example.hackathon.domain.recommendation.dto.EffectRecommendationDtos.RecommendEffectsResponse;
import com.example.hackathon.domain.recommendation.service.EffectRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI 효과 추천", description = "생존법 제목, 설명, 추천 상황을 기반으로 Gemini가 카드 효과를 임시 추천하는 API")
@RestController
@RequestMapping("/api/effect-recommendations")
public class EffectRecommendationController {

    private final EffectRecommendationService effectRecommendationService;

    public EffectRecommendationController(EffectRecommendationService effectRecommendationService) {
        this.effectRecommendationService = effectRecommendationService;
    }

    @Operation(
            summary = "카드 효과 AI 추천",
            description = "카드 저장 없이 제목, 설명, 추천 상황으로 효과 1~3개와 level을 추천합니다. 사용자는 추천 결과를 수정한 뒤 기존 카드 저장 API를 호출합니다."
    )
    @PostMapping
    public RecommendEffectsResponse recommendEffects(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Valid @RequestBody RecommendEffectsRequest request
    ) {
        return effectRecommendationService.recommendEffects(userId, request);
    }
}
