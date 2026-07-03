package com.example.hackathon.domain.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.effect.repository.EffectTypeRepository;
import com.example.hackathon.domain.recommendation.dto.EffectRecommendationDtos.RecommendEffectsRequest;
import com.example.hackathon.domain.recommendation.dto.EffectRecommendationDtos.RecommendEffectsResponse;
import com.example.hackathon.domain.user.service.UserReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EffectRecommendationServiceTest {

    @Test
    void returnsDefaultRecommendationsWhenGeminiScoresAreEmpty() {
        GeminiEffectRecommendationClient geminiClient = mock(GeminiEffectRecommendationClient.class);
        EffectTypeRepository effectTypeRepository = mock(EffectTypeRepository.class);
        UserReader userReader = mock(UserReader.class);
        EffectRecommendationService service = new EffectRecommendationService(
                geminiClient,
                effectTypeRepository,
                userReader
        );
        List<EffectType> effectTypes = List.of(
                effectType("COOLING", "냉각력", 1),
                effectType("MENTAL", "정신력", 2),
                effectType("STAMINA", "체력", 3),
                effectType("MONEY", "자본력", 4),
                effectType("PATIENCE", "인내력", 5)
        );
        when(effectTypeRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(effectTypes);
        when(geminiClient.recommendScores("제목", "설명", "상황")).thenReturn(Map.of());

        RecommendEffectsResponse response = service.recommendEffects(
                1L,
                new RecommendEffectsRequest("제목", "설명", "상황")
        );

        assertThat(response.effects())
                .extracting(effect -> effect.code())
                .containsExactly("COOLING", "MENTAL", "STAMINA");
        assertThat(response.scores())
                .extracting(score -> score.score())
                .containsOnly((short) 2);
    }

    private EffectType effectType(String code, String name, int displayOrder) {
        return new EffectType(code, name, code.toLowerCase(), "COLOR", (short) displayOrder);
    }
}
