package com.example.hackathon.domain.exchange.service;

import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.PrimaryEffectResponse;
import com.example.hackathon.domain.card.entity.SurvivalCard;
import com.example.hackathon.domain.collection.entity.CollectionCard;
import com.example.hackathon.domain.collection.entity.CollectionSource;
import com.example.hackathon.domain.collection.repository.CollectionCardRepository;
import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.exchange.controller.dto.CardExchangeDtos.ReceivedCardResponse;
import com.example.hackathon.domain.exchange.controller.dto.CardExchangeDtos.CardExchangeResponse;
import com.example.hackathon.domain.exchange.controller.dto.CardExchangeDtos.SentCardResponse;
import com.example.hackathon.domain.exchange.entity.CardExchange;
import com.example.hackathon.domain.exchange.repository.CardExchangeRepository;
import com.example.hackathon.domain.user.service.UserReader;
import com.example.hackathon.global.exception.ForbiddenException;
import com.example.hackathon.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CardExchangeService {

    private final CardExchangeRepository cardExchangeRepository;
    private final CollectionCardRepository collectionCardRepository;
    private final UserReader userReader;

    public CardExchangeService(
            CardExchangeRepository cardExchangeRepository,
            CollectionCardRepository collectionCardRepository,
            UserReader userReader
    ) {
        this.cardExchangeRepository = cardExchangeRepository;
        this.collectionCardRepository = collectionCardRepository;
        this.userReader = userReader;
    }

    public CardExchangeResponse getExchange(Long userId, Long exchangeId) {
        userReader.getById(userId);
        CardExchange exchange = cardExchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new NotFoundException("카드 교환 결과를 찾을 수 없습니다."));

        if (!exchange.getRequesterUser().getId().equals(userId)) {
            throw new ForbiddenException("내가 요청한 카드 교환 결과만 조회할 수 있습니다.");
        }

        SurvivalCard sentCard = exchange.getRequesterMailing().getCard();
        SurvivalCard receivedCard = exchange.getSelectedMailing().getCard();

        return new CardExchangeResponse(
                exchange.getId(),
                exchange.getExchangedAt(),
                toSentCardResponse(sentCard),
                toReceivedCardResponse(userId, exchange.getId(), receivedCard)
        );
    }

    private SentCardResponse toSentCardResponse(SurvivalCard card) {
        return new SentCardResponse(
                card.getId(),
                card.getTitle()
        );
    }

    private ReceivedCardResponse toReceivedCardResponse(Long userId, Long exchangeId, SurvivalCard card) {
        Long collectionCardId = collectionCardRepository
                .findByExchange_IdAndUser_IdAndCard_IdAndSource(
                        exchangeId,
                        userId,
                        card.getId(),
                        CollectionSource.RECEIVED
                )
                .map(CollectionCard::getId)
                .orElse(null);

        return new ReceivedCardResponse(
                collectionCardId,
                card.getId(),
                card.getTitle(),
                card.getDescription(),
                card.getRecommendedSituation(),
                card.getDifficulty(),
                toPrimaryEffectResponse(card.getPrimaryEffectType())
        );
    }

    private PrimaryEffectResponse toPrimaryEffectResponse(EffectType effectType) {
        return new PrimaryEffectResponse(
                effectType.getId(),
                effectType.getName(),
                effectType.getColor(),
                effectType.getIcon()
        );
    }
}
