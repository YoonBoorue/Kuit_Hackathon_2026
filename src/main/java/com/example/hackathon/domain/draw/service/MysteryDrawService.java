package com.example.hackathon.domain.draw.service;

import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.CardEffectResponse;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.PrimaryEffectResponse;
import com.example.hackathon.domain.card.entity.CardEffect;
import com.example.hackathon.domain.card.entity.SurvivalCard;
import com.example.hackathon.domain.card.repository.CardEffectRepository;
import com.example.hackathon.domain.collection.entity.CollectionCard;
import com.example.hackathon.domain.collection.entity.CollectionSource;
import com.example.hackathon.domain.collection.repository.CollectionCardRepository;
import com.example.hackathon.domain.draw.controller.dto.MysteryDrawDtos.MysteryDrawOptionResponse;
import com.example.hackathon.domain.draw.controller.dto.MysteryDrawDtos.MysteryDrawResponse;
import com.example.hackathon.domain.draw.controller.dto.MysteryDrawDtos.ReceivedCardResponse;
import com.example.hackathon.domain.draw.controller.dto.MysteryDrawDtos.SelectMysteryDrawRequest;
import com.example.hackathon.domain.draw.controller.dto.MysteryDrawDtos.SelectMysteryDrawResponse;
import com.example.hackathon.domain.draw.entity.MysteryDrawOption;
import com.example.hackathon.domain.draw.entity.MysteryDrawSession;
import com.example.hackathon.domain.draw.repository.MysteryDrawOptionRepository;
import com.example.hackathon.domain.draw.repository.MysteryDrawSessionRepository;
import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.exchange.entity.CardExchange;
import com.example.hackathon.domain.exchange.repository.CardExchangeRepository;
import com.example.hackathon.domain.mailing.entity.CardMailing;
import com.example.hackathon.domain.mailing.repository.CardMailingRepository;
import com.example.hackathon.domain.user.service.UserReader;
import com.example.hackathon.global.exception.ConflictException;
import com.example.hackathon.global.exception.ForbiddenException;
import com.example.hackathon.global.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MysteryDrawService {

    private final MysteryDrawSessionRepository mysteryDrawSessionRepository;
    private final MysteryDrawOptionRepository mysteryDrawOptionRepository;
    private final CardExchangeRepository cardExchangeRepository;
    private final CollectionCardRepository collectionCardRepository;
    private final CardEffectRepository cardEffectRepository;
    private final CardMailingRepository cardMailingRepository;
    private final UserReader userReader;

    public MysteryDrawService(
            MysteryDrawSessionRepository mysteryDrawSessionRepository,
            MysteryDrawOptionRepository mysteryDrawOptionRepository,
            CardExchangeRepository cardExchangeRepository,
            CollectionCardRepository collectionCardRepository,
            CardEffectRepository cardEffectRepository,
            CardMailingRepository cardMailingRepository,
            UserReader userReader
    ) {
        this.mysteryDrawSessionRepository = mysteryDrawSessionRepository;
        this.mysteryDrawOptionRepository = mysteryDrawOptionRepository;
        this.cardExchangeRepository = cardExchangeRepository;
        this.collectionCardRepository = collectionCardRepository;
        this.cardEffectRepository = cardEffectRepository;
        this.cardMailingRepository = cardMailingRepository;
        this.userReader = userReader;
    }

    public MysteryDrawResponse getMysteryDraw(Long userId, Long mysteryDrawId) {
        MysteryDrawSession session = getSessionForUser(userId, mysteryDrawId);
        List<MysteryDrawOptionResponse> options = mysteryDrawOptionRepository
                .findAllByMysteryDrawSession_IdOrderByPositionAsc(mysteryDrawId)
                .stream()
                .map(option -> toOptionResponse(session, option))
                .toList();

        return new MysteryDrawResponse(session.getId(), session.getStatus(), options);
    }

    @Transactional
    public SelectMysteryDrawResponse selectMysteryCard(
            Long userId,
            Long mysteryDrawId,
            SelectMysteryDrawRequest request
    ) {
        MysteryDrawSession session = getSessionForUserForUpdate(userId, mysteryDrawId);
        MysteryDrawOption selectedOption = mysteryDrawOptionRepository
                .findByIdAndMysteryDrawSession_Id(request.optionId(), mysteryDrawId)
                .orElseThrow(() -> new NotFoundException("해당 미스터리 뽑기에 포함된 옵션을 찾을 수 없습니다."));

        if (!session.isOpen()) {
            throw new ConflictException("이미 완료되었거나 선택할 수 없는 미스터리 뽑기입니다.");
        }

        Map<Long, CardMailing> lockedMailings = lockMailings(
                session.getOfferedMailing().getId(),
                selectedOption.getCandidateMailing().getId()
        );
        CardMailing requesterMailing = lockedMailings.get(session.getOfferedMailing().getId());
        CardMailing selectedMailing = lockedMailings.get(selectedOption.getCandidateMailing().getId());

        if (!requesterMailing.isWaiting()) {
            throw new ConflictException("내가 발송한 우편이 더 이상 교환 대기 상태가 아닙니다.");
        }
        if (!selectedMailing.isWaiting()) {
            throw new ConflictException("이미 다른 사용자가 선택한 카드입니다. 미스터리 뽑기를 다시 조회해주세요.");
        }
        if (selectedMailing.getSenderUser().getId().equals(userId)) {
            throw new ForbiddenException("내가 보낸 우편은 선택할 수 없습니다.");
        }

        OffsetDateTime exchangedAt = OffsetDateTime.now();
        CardExchange exchange = cardExchangeRepository.save(new CardExchange(
                session,
                session.getUser(),
                requesterMailing,
                selectedMailing.getSenderUser(),
                selectedMailing,
                exchangedAt
        ));

        requesterMailing.markMatched(exchangedAt);
        selectedMailing.markMatched(exchangedAt);
        session.complete(exchangedAt);

        CollectionCard requesterReceivedCard = collectionCardRepository.save(new CollectionCard(
                session.getUser(),
                selectedMailing.getCard(),
                exchange,
                CollectionSource.RECEIVED,
                exchangedAt
        ));

        collectionCardRepository.save(new CollectionCard(
                selectedMailing.getSenderUser(),
                requesterMailing.getCard(),
                exchange,
                CollectionSource.RECEIVED,
                exchangedAt
        ));

        return new SelectMysteryDrawResponse(
                exchange.getId(),
                toReceivedCardResponse(requesterReceivedCard)
        );
    }

    private MysteryDrawSession getSessionForUser(Long userId, Long mysteryDrawId) {
        userReader.getById(userId);
        return mysteryDrawSessionRepository.findByIdAndUser_Id(mysteryDrawId, userId)
                .orElseThrow(() -> new NotFoundException("미스터리 뽑기를 찾을 수 없습니다."));
    }

    private MysteryDrawSession getSessionForUserForUpdate(Long userId, Long mysteryDrawId) {
        userReader.getById(userId);
        return mysteryDrawSessionRepository.findByIdAndUserIdForUpdate(mysteryDrawId, userId)
                .orElseThrow(() -> new NotFoundException("미스터리 뽑기를 찾을 수 없습니다."));
    }

    private Map<Long, CardMailing> lockMailings(Long requesterMailingId, Long selectedMailingId) {
        return Stream.of(requesterMailingId, selectedMailingId)
                .distinct()
                .sorted()
                .map(this::getMailingForUpdate)
                .collect(Collectors.toMap(CardMailing::getId, Function.identity()));
    }

    private CardMailing getMailingForUpdate(Long mailingId) {
        return cardMailingRepository.findByIdForUpdate(mailingId)
                .orElseThrow(() -> new NotFoundException("우편을 찾을 수 없습니다."));
    }

    private MysteryDrawOptionResponse toOptionResponse(MysteryDrawSession session, MysteryDrawOption option) {
        CardMailing mailing = option.getCandidateMailing();
        EffectType primaryEffectType = mailing.getCard().getPrimaryEffectType();
        return new MysteryDrawOptionResponse(
                option.getId(),
                option.getPosition(),
                primaryEffectType.getColor(),
                toPrimaryEffectResponse(primaryEffectType),
                session.isOpen() && mailing.isWaiting()
        );
    }

    private ReceivedCardResponse toReceivedCardResponse(CollectionCard collectionCard) {
        SurvivalCard card = collectionCard.getCard();
        return new ReceivedCardResponse(
                collectionCard.getId(),
                card.getId(),
                card.getTitle(),
                card.getDescription(),
                card.getRecommendedSituation(),
                card.getDifficulty(),
                card.getImageUrl(),
                resolveReceivedMessage(collectionCard),
                card.getAuthorUser().getId(),
                card.getAuthorUser().getNickname(),
                toPrimaryEffectResponse(card.getPrimaryEffectType()),
                toCardEffectResponses(card.getId()),
                card.getCreatedAt()
        );
    }

    private String resolveReceivedMessage(CollectionCard collectionCard) {
        CardExchange exchange = collectionCard.getExchange();
        if (exchange == null) {
            return null;
        }
        Long userId = collectionCard.getUser().getId();
        if (exchange.getRequesterUser().getId().equals(userId)) {
            return exchange.getSelectedMailing().getMessage();
        }
        if (exchange.getSelectedUser().getId().equals(userId)) {
            return exchange.getRequesterMailing().getMessage();
        }
        return null;
    }

    private List<CardEffectResponse> toCardEffectResponses(Long cardId) {
        return cardEffectRepository.findAllByCard_IdOrderByDisplayOrderAsc(cardId)
                .stream()
                .map(this::toCardEffectResponse)
                .toList();
    }

    private CardEffectResponse toCardEffectResponse(CardEffect cardEffect) {
        EffectType effectType = cardEffect.getEffectType();
        return new CardEffectResponse(
                effectType.getId(),
                effectType.getName(),
                cardEffect.getLevel()
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
