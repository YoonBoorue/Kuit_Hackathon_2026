package com.example.hackathon.domain.exchange.controller.dto;

import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.PrimaryEffectResponse;
import java.time.OffsetDateTime;

public final class CardExchangeDtos {

    private CardExchangeDtos() {
    }

    public record CardExchangeResponse(
            Long exchangeId,
            OffsetDateTime exchangedAt,
            SentCardResponse sentCard,
            ReceivedCardResponse receivedCard
    ) {
    }

    public record SentCardResponse(
            Long cardId,
            String title
    ) {
    }

    public record ReceivedCardResponse(
            Long collectionCardId,
            Long cardId,
            String title,
            String description,
            String recommendedSituation,
            Short difficulty,
            PrimaryEffectResponse primaryEffect
    ) {
    }
}
