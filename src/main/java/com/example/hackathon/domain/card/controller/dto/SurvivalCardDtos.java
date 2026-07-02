package com.example.hackathon.domain.card.controller.dto;

import com.example.hackathon.domain.card.entity.CardStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.List;

public final class SurvivalCardDtos {

    private SurvivalCardDtos() {
    }

    public record CreateSurvivalCardRequest(
            @NotBlank(message = "제목은 필수입니다.")
            @Size(max = 20, message = "제목은 20자 이하여야 합니다.")
            String title,

            @NotBlank(message = "설명은 필수입니다.")
            @Size(max = 100, message = "설명은 100자 이하여야 합니다.")
            String description,

            @NotBlank(message = "추천 상황은 필수입니다.")
            @Size(max = 20, message = "추천 상황은 20자 이하여야 합니다.")
            String recommendedSituation,

            @NotNull(message = "난이도는 필수입니다.")
            @Min(value = 1, message = "난이도는 1 이상이어야 합니다.")
            @Max(value = 5, message = "난이도는 5 이하여야 합니다.")
            Short difficulty,

            @NotNull(message = "대표 효과 유형 ID는 필수입니다.")
            Long primaryEffectTypeId,

            @Valid
            @NotEmpty(message = "카드 효과는 1개 이상이어야 합니다.")
            @Size(max = 3, message = "카드 효과는 최대 3개까지 가능합니다.")
            List<EffectRequest> effects,

            boolean favorite,

            Long folderId,

            @Size(max = 50, message = "새 폴더 이름은 50자 이하여야 합니다.")
            String newFolderName,

            @Size(max = 30, message = "새 폴더 색상은 30자 이하여야 합니다.")
            String newFolderColor
    ) {
    }

    public record EffectRequest(
            @NotNull(message = "효과 유형 ID는 필수입니다.")
            Long effectTypeId,

            @NotNull(message = "효과 레벨은 필수입니다.")
            @Min(value = 1, message = "효과 레벨은 1 이상이어야 합니다.")
            @Max(value = 5, message = "효과 레벨은 5 이하여야 합니다.")
            Short level,

            @NotNull(message = "효과 표시 순서는 필수입니다.")
            @Min(value = 1, message = "효과 표시 순서는 1 이상이어야 합니다.")
            @Max(value = 3, message = "효과 표시 순서는 3 이하여야 합니다.")
            Short displayOrder
    ) {
    }

    public record CreateSurvivalCardResponse(
            Long cardId,
            Long collectionCardId,
            CardStatus status,
            String message
    ) {
    }

    public record PrimaryEffectResponse(
            Long effectTypeId,
            String name,
            String color,
            String icon
    ) {
    }

    public record CardEffectResponse(
            Long effectTypeId,
            String name,
            Short level
    ) {
    }

    public record SurvivalCardResponse(
            Long cardId,
            String title,
            String description,
            String recommendedSituation,
            Short difficulty,
            CardStatus status,
            PrimaryEffectResponse primaryEffect,
            List<CardEffectResponse> effects,
            OffsetDateTime createdAt
    ) {
    }
}
