package com.example.hackathon.domain.exchange.dto;

import com.example.hackathon.domain.common.dto.CardDtos.PrimaryEffectResponse;
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
            String title,
            String imageUrl
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
            PrimaryEffectResponse primaryEffect
    ) {
    }
}
