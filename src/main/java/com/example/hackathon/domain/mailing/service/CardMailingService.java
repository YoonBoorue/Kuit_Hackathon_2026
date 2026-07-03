package com.example.hackathon.domain.mailing.service;

import com.example.hackathon.domain.card.entity.CardStatus;
import com.example.hackathon.domain.card.entity.SurvivalCard;
import com.example.hackathon.domain.card.repository.SurvivalCardRepository;
import com.example.hackathon.domain.draw.entity.MysteryDrawOption;
import com.example.hackathon.domain.draw.entity.MysteryDrawSession;
import com.example.hackathon.domain.draw.repository.MysteryDrawOptionRepository;
import com.example.hackathon.domain.draw.repository.MysteryDrawSessionRepository;
import com.example.hackathon.domain.mailing.dto.CardMailingDtos.CardMailingCreateResponse;
import com.example.hackathon.domain.mailing.dto.CardMailingDtos.CardMailingResponse;
import com.example.hackathon.domain.mailing.dto.CardMailingDtos.CreateCardMailingRequest;
import com.example.hackathon.domain.mailing.entity.CardMailing;
import com.example.hackathon.domain.mailing.entity.MailingStatus;
import com.example.hackathon.domain.mailing.repository.CardMailingRepository;
import com.example.hackathon.domain.user.entity.User;
import com.example.hackathon.domain.user.service.UserReader;
import com.example.hackathon.global.exception.ConflictException;
import com.example.hackathon.global.exception.ForbiddenException;
import com.example.hackathon.global.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CardMailingService {

    private static final int MYSTERY_DRAW_OPTION_COUNT = 4;

    private final CardMailingRepository cardMailingRepository;
    private final SurvivalCardRepository survivalCardRepository;
    private final MysteryDrawSessionRepository mysteryDrawSessionRepository;
    private final MysteryDrawOptionRepository mysteryDrawOptionRepository;
    private final UserReader userReader;

    public CardMailingService(
            CardMailingRepository cardMailingRepository,
            SurvivalCardRepository survivalCardRepository,
            MysteryDrawSessionRepository mysteryDrawSessionRepository,
            MysteryDrawOptionRepository mysteryDrawOptionRepository,
            UserReader userReader
    ) {
        this.cardMailingRepository = cardMailingRepository;
        this.survivalCardRepository = survivalCardRepository;
        this.mysteryDrawSessionRepository = mysteryDrawSessionRepository;
        this.mysteryDrawOptionRepository = mysteryDrawOptionRepository;
        this.userReader = userReader;
    }

    @Transactional
    public CardMailingCreateResponse createMailing(Long userId, CreateCardMailingRequest request) {
        User user = userReader.getById(userId);
        SurvivalCard card = survivalCardRepository.findById(request.cardId())
                .orElseThrow(() -> new NotFoundException("생존 카드를 찾을 수 없습니다."));

        validateCardCanBeMailed(userId, card);
        if (cardMailingRepository.existsByCard_Id(card.getId())) {
            throw new ConflictException("이미 발송된 카드입니다.");
        }

        CardMailing mailing = cardMailingRepository.save(new CardMailing(
                user,
                card,
                normalizeMessage(request.message()),
                OffsetDateTime.now()
        ));
        card.markSent();

        List<CardMailing> candidates = findMysteryDrawCandidates(userId, mailing.getId());
        MysteryDrawSession mysteryDrawSession = mysteryDrawSessionRepository.save(new MysteryDrawSession(user, mailing));
        createMysteryDrawOptions(mysteryDrawSession, candidates);

        return new CardMailingCreateResponse(
                mailing.getId(),
                card.getId(),
                mailing.getStatus(),
                mysteryDrawSession.getId()
        );
    }

    public List<CardMailingResponse> getMyMailings(Long userId, MailingStatus status) {
        userReader.getById(userId);
        List<CardMailing> mailings = status == null
                ? cardMailingRepository.findAllBySenderUser_Id(userId)
                : cardMailingRepository.findAllBySenderUser_IdAndStatus(userId, status);

        return mailings.stream()
                .map(this::toMailingResponse)
                .toList();
    }

    private void validateCardCanBeMailed(Long userId, SurvivalCard card) {
        if (!card.isWrittenBy(userId)) {
            throw new ForbiddenException("내가 작성한 카드만 발송할 수 있습니다.");
        }
        if (card.getStatus() != CardStatus.UNSENT) {
            throw new ConflictException("발송 가능한 상태의 카드가 아닙니다.");
        }
    }

    private List<CardMailing> findMysteryDrawCandidates(Long userId, Long mailingId) {
        List<CardMailing> candidates = cardMailingRepository.findRandomCandidates(
                MailingStatus.WAITING,
                userId,
                mailingId,
                PageRequest.of(0, MYSTERY_DRAW_OPTION_COUNT)
        );

        if (candidates.size() < MYSTERY_DRAW_OPTION_COUNT) {
            throw new ConflictException("미스터리 뽑기 후보 카드가 부족합니다.");
        }
        return candidates;
    }

    private List<MysteryDrawOption> createMysteryDrawOptions(
            MysteryDrawSession mysteryDrawSession,
            List<CardMailing> candidates
    ) {
        List<MysteryDrawOption> options = new ArrayList<>();
        for (int index = 0; index < candidates.size(); index++) {
            options.add(new MysteryDrawOption(
                    mysteryDrawSession,
                    candidates.get(index),
                    (short) (index + 1)
            ));
        }
        return mysteryDrawOptionRepository.saveAll(options);
    }

    private String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        return message.trim();
    }

    private CardMailingResponse toMailingResponse(CardMailing mailing) {
        return new CardMailingResponse(
                mailing.getId(),
                mailing.getCard().getId(),
                mailing.getCard().getImageUrl(),
                mailing.getMessage(),
                mailing.getStatus(),
                mailing.getSentAt(),
                mailing.getMatchedAt()
        );
    }

}
