package com.example.hackathon.domain.draw.dto;

import com.example.hackathon.domain.common.dto.CardDtos.CardEffectResponse;
import com.example.hackathon.domain.common.dto.CardDtos.PrimaryEffectResponse;
import com.example.hackathon.domain.draw.entity.MysteryDrawStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public final class MysteryDrawDtos {

    private MysteryDrawDtos() {
    }

    public record MysteryDrawResponse(
            Long mysteryDrawId,
            MysteryDrawStatus status,
            List<MysteryDrawOptionResponse> options
    ) {
    }

    public record MysteryDrawOptionResponse(
            Long optionId,
            Short position,
            String cardBackColor,
            PrimaryEffectResponse primaryEffect,
            boolean selectable
    ) {
    }

    public record SelectMysteryDrawRequest(
            @NotNull(message = "선택할 옵션 ID는 필수입니다.")
            Long optionId
    ) {
    }

    public record SelectMysteryDrawResponse(
            Long exchangeId,
            ReceivedCardResponse receivedCard
    ) {
    }

    public record ReceivedCardResponse(
            Long collectionCardId,
            Long cardId,
            String title,
            String description,
            String recommendedSituation,
            Short difficulty,
            String imageUrl,
            String message,
            Long authorUserId,
            String authorNickname,
            PrimaryEffectResponse primaryEffect,
            List<CardEffectResponse> effects,
            LocalDateTime createdAt
    ) {
    }
}
