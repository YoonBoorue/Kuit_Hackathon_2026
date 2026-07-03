package com.example.hackathon.domain.recommendation.service;

import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.effect.repository.EffectTypeRepository;
import com.example.hackathon.domain.recommendation.controller.dto.EffectRecommendationDtos.EffectScoreResponse;
import com.example.hackathon.domain.recommendation.controller.dto.EffectRecommendationDtos.RecommendEffectsRequest;
import com.example.hackathon.domain.recommendation.controller.dto.EffectRecommendationDtos.RecommendEffectsResponse;
import com.example.hackathon.domain.recommendation.controller.dto.EffectRecommendationDtos.RecommendedEffectResponse;
import com.example.hackathon.domain.user.service.UserReader;
import com.example.hackathon.global.exception.ConflictException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EffectRecommendationService {

    private static final Set<String> REQUIRED_CODES = Set.of(
            "COOLING",
            "MENTAL",
            "STAMINA",
            "MONEY",
            "PATIENCE"
    );

    private final GeminiEffectRecommendationClient geminiClient;
    private final EffectTypeRepository effectTypeRepository;
    private final UserReader userReader;

    public EffectRecommendationService(
            GeminiEffectRecommendationClient geminiClient,
            EffectTypeRepository effectTypeRepository,
            UserReader userReader
    ) {
        this.geminiClient = geminiClient;
        this.effectTypeRepository = effectTypeRepository;
        this.userReader = userReader;
    }

    public RecommendEffectsResponse recommendEffects(Long userId, RecommendEffectsRequest request) {
        userReader.getById(userId);
        List<EffectType> effectTypes = getEffectTypes();

        String title = request.title().trim();
        String description = request.description().trim();
        Map<String, Integer> geminiScores = geminiClient.recommendScores(title, description);
        Map<String, Short> normalizedScores = normalizeScores(geminiScores, title + " " + description);

        List<RecommendedEffectResponse> recommendedEffects = effectTypes.stream()
                .sorted(recommendationComparator(normalizedScores))
                .limit(3)
                .filter(effectType -> normalizedScores.get(effectType.getCode()) > 1)
                .toList()
                .stream()
                .limit(3)
                .map(effectType -> toRecommendedEffectResponse(
                        effectType,
                        normalizedScores.get(effectType.getCode()),
                        (short) 0
                ))
                .toList();

        if (recommendedEffects.isEmpty()) {
            EffectType fallbackEffectType = effectTypes.stream()
                    .min(Comparator.comparing(EffectType::getDisplayOrder))
                    .orElseThrow(() -> new ConflictException("효과 타입 기본 데이터가 필요합니다."));
            recommendedEffects = List.of(toRecommendedEffectResponse(
                    fallbackEffectType,
                    normalizedScores.getOrDefault(fallbackEffectType.getCode(), (short) 1),
                    (short) 1
            ));
        } else {
            recommendedEffects = withDisplayOrder(recommendedEffects);
        }

        List<EffectScoreResponse> scores = effectTypes.stream()
                .map(effectType -> new EffectScoreResponse(
                        effectType.getId(),
                        effectType.getCode(),
                        effectType.getName(),
                        normalizedScores.get(effectType.getCode())
                ))
                .toList();

        return new RecommendEffectsResponse(
                recommendedEffects.getFirst().effectTypeId(),
                recommendedEffects,
                scores
        );
    }

    private List<EffectType> getEffectTypes() {
        List<EffectType> effectTypes = effectTypeRepository.findAllByOrderByDisplayOrderAsc();
        Set<String> codes = effectTypes.stream()
                .map(EffectType::getCode)
                .collect(Collectors.toSet());
        if (!codes.containsAll(REQUIRED_CODES)) {
            throw new ConflictException("효과 타입 기본 데이터가 필요합니다.");
        }
        return effectTypes.stream()
                .filter(effectType -> REQUIRED_CODES.contains(effectType.getCode()))
                .toList();
    }

    private Map<String, Short> normalizeScores(Map<String, Integer> geminiScores, String text) {
        Map<String, Short> normalizedScores = new LinkedHashMap<>();
        for (String code : REQUIRED_CODES) {
            int fallbackScore = fallbackScore(code, text);
            int score = geminiScores.getOrDefault(code, fallbackScore);
            normalizedScores.put(code, (short) clamp(score, 1, 5));
        }
        return normalizedScores;
    }

    private Comparator<EffectType> recommendationComparator(Map<String, Short> scores) {
        return Comparator
                .comparing((EffectType effectType) -> scores.get(effectType.getCode()), Comparator.reverseOrder())
                .thenComparing(EffectType::getDisplayOrder);
    }

    private List<RecommendedEffectResponse> withDisplayOrder(List<RecommendedEffectResponse> effects) {
        short displayOrder = 1;
        java.util.ArrayList<RecommendedEffectResponse> orderedEffects = new java.util.ArrayList<>();
        for (RecommendedEffectResponse effect : effects) {
            orderedEffects.add(new RecommendedEffectResponse(
                    effect.effectTypeId(),
                    effect.code(),
                    effect.name(),
                    effect.icon(),
                    effect.color(),
                    effect.level(),
                    displayOrder++
            ));
        }
        return orderedEffects;
    }

    private RecommendedEffectResponse toRecommendedEffectResponse(
            EffectType effectType,
            Short level,
            Short displayOrder
    ) {
        return new RecommendedEffectResponse(
                effectType.getId(),
                effectType.getCode(),
                effectType.getName(),
                effectType.getIcon(),
                effectType.getColor(),
                level,
                displayOrder
        );
    }

    private int fallbackScore(String code, String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);
        return switch (code) {
            case "COOLING" -> containsAny(lowerText, "시원", "얼음", "냉", "그늘", "물", "더위", "선풍", "에어컨") ? 4 : 2;
            case "MENTAL" -> containsAny(lowerText, "기분", "멘탈", "스트레스", "짜증", "휴식", "힐링", "상쾌") ? 4 : 2;
            case "STAMINA" -> containsAny(lowerText, "체력", "피로", "잠", "회복", "운동", "몸", "쉬") ? 4 : 2;
            case "MONEY" -> containsAny(lowerText, "돈", "절약", "저렴", "가성비", "무료", "할인", "비용") ? 4 : 2;
            case "PATIENCE" -> containsAny(lowerText, "버티", "인내", "참", "견디", "지속", "루틴") ? 4 : 2;
            default -> 2;
        };
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private int clamp(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }
}
