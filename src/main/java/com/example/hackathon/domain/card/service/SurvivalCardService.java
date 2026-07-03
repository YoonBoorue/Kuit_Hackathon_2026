package com.example.hackathon.domain.card.service;

import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.CardEffectResponse;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.CreateSurvivalCardRequest;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.CreateSurvivalCardResponse;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.EffectRequest;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.PrimaryEffectResponse;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.SurvivalCardResponse;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.UpdateCardImageRequest;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.UpdateCardImageResponse;
import com.example.hackathon.domain.card.entity.CardEffect;
import com.example.hackathon.domain.card.entity.CardStatus;
import com.example.hackathon.domain.card.entity.SurvivalCard;
import com.example.hackathon.domain.card.repository.CardEffectRepository;
import com.example.hackathon.domain.card.repository.SurvivalCardRepository;
import com.example.hackathon.domain.collection.entity.CollectionCard;
import com.example.hackathon.domain.collection.entity.CollectionSource;
import com.example.hackathon.domain.collection.repository.CollectionCardRepository;
import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.effect.repository.EffectTypeRepository;
import com.example.hackathon.domain.folder.entity.Folder;
import com.example.hackathon.domain.folder.entity.FolderCard;
import com.example.hackathon.domain.folder.repository.FolderCardRepository;
import com.example.hackathon.domain.folder.repository.FolderRepository;
import com.example.hackathon.domain.user.entity.User;
import com.example.hackathon.domain.user.service.UserReader;
import com.example.hackathon.global.exception.BadRequestException;
import com.example.hackathon.global.exception.ConflictException;
import com.example.hackathon.global.exception.ForbiddenException;
import com.example.hackathon.global.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SurvivalCardService {

    private final SurvivalCardRepository survivalCardRepository;
    private final CardEffectRepository cardEffectRepository;
    private final EffectTypeRepository effectTypeRepository;
    private final CollectionCardRepository collectionCardRepository;
    private final FolderRepository folderRepository;
    private final FolderCardRepository folderCardRepository;
    private final UserReader userReader;

    public SurvivalCardService(
            SurvivalCardRepository survivalCardRepository,
            CardEffectRepository cardEffectRepository,
            EffectTypeRepository effectTypeRepository,
            CollectionCardRepository collectionCardRepository,
            FolderRepository folderRepository,
            FolderCardRepository folderCardRepository,
            UserReader userReader
    ) {
        this.survivalCardRepository = survivalCardRepository;
        this.cardEffectRepository = cardEffectRepository;
        this.effectTypeRepository = effectTypeRepository;
        this.collectionCardRepository = collectionCardRepository;
        this.folderRepository = folderRepository;
        this.folderCardRepository = folderCardRepository;
        this.userReader = userReader;
    }

    @Transactional
    public CreateSurvivalCardResponse createCard(Long userId, CreateSurvivalCardRequest request) {
        User user = userReader.getById(userId);
        validateEffects(request);

        Map<Long, EffectType> effectTypes = getEffectTypesById(request.effects());
        EffectType primaryEffectType = effectTypes.get(request.primaryEffectTypeId());
        if (primaryEffectType == null) {
            throw new BadRequestException("대표 효과는 카드 효과 목록에 포함되어야 합니다.");
        }

        SurvivalCard card = survivalCardRepository.save(new SurvivalCard(
                user,
                request.title().trim(),
                request.description().trim(),
                request.recommendedSituation().trim(),
                request.difficulty(),
                primaryEffectType,
                request.imageUrl(),
                request.imageKey()
        ));

        List<CardEffect> cardEffects = request.effects()
                .stream()
                .map(effect -> new CardEffect(
                        card,
                        effectTypes.get(effect.effectTypeId()),
                        effect.level(),
                        effect.displayOrder()
                ))
                .toList();
        cardEffectRepository.saveAll(cardEffects);

        CollectionCard collectionCard = new CollectionCard(
                user,
                card,
                null,
                CollectionSource.CREATED,
                OffsetDateTime.now()
        );
        collectionCard.changeFavorite(request.favorite());
        collectionCardRepository.save(collectionCard);
        saveToSelectedFolder(user, collectionCard, request);

        return new CreateSurvivalCardResponse(
                card.getId(),
                collectionCard.getId(),
                card.getStatus(),
                "카드가 저장되었습니다."
        );
    }

    public List<SurvivalCardResponse> getMyCards(Long userId, CardStatus status) {
        userReader.getById(userId);
        List<SurvivalCard> cards = status == null
                ? survivalCardRepository.findAllByAuthorUser_Id(userId)
                : survivalCardRepository.findAllByAuthorUser_IdAndStatus(userId, status);

        return cards.stream()
                .map(this::toResponse)
                .toList();
    }

    public SurvivalCardResponse getCard(Long cardId) {
        SurvivalCard card = survivalCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("생존 카드를 찾을 수 없습니다."));
        if (card.getStatus() == CardStatus.DELETED) {
            throw new NotFoundException("생존 카드를 찾을 수 없습니다.");
        }
        return toResponse(card);
    }

    @Transactional
    public UpdateCardImageResponse updateCardImage(Long userId, Long cardId, UpdateCardImageRequest request) {
        userReader.getById(userId);
        SurvivalCard card = survivalCardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("생존 카드를 찾을 수 없습니다."));
        if (card.getStatus() == CardStatus.DELETED) {
            throw new NotFoundException("생존 카드를 찾을 수 없습니다.");
        }
        if (!card.isWrittenBy(userId)) {
            throw new ForbiddenException("내가 만든 카드만 이미지를 변경할 수 있습니다.");
        }

        if (card.getStatus() != CardStatus.UNSENT) {
            throw new ConflictException("발송 전 카드만 이미지를 변경할 수 있습니다.");
        }

        card.updateImage(request.imageUrl(), request.imageKey());
        return new UpdateCardImageResponse(
                card.getId(),
                card.getImageUrl(),
                card.getImageKey(),
                "카드 이미지가 변경되었습니다."
        );
    }

    private void validateEffects(CreateSurvivalCardRequest request) {
        List<EffectRequest> effects = request.effects();
        Set<Long> effectTypeIds = new HashSet<>();
        Set<Short> displayOrders = new HashSet<>();

        for (EffectRequest effect : effects) {
            if (!effectTypeIds.add(effect.effectTypeId())) {
                throw new BadRequestException("카드 효과 유형은 중복될 수 없습니다.");
            }
            if (!displayOrders.add(effect.displayOrder())) {
                throw new BadRequestException("카드 효과 표시 순서는 중복될 수 없습니다.");
            }
        }

        if (!effectTypeIds.contains(request.primaryEffectTypeId())) {
            throw new BadRequestException("대표 효과는 카드 효과 목록에 포함되어야 합니다.");
        }
    }

    private void saveToSelectedFolder(
            User user,
            CollectionCard collectionCard,
            CreateSurvivalCardRequest request
    ) {
        boolean hasFolderId = request.folderId() != null;
        boolean hasNewFolderName = request.newFolderName() != null && !request.newFolderName().isBlank();

        if (hasFolderId && hasNewFolderName) {
            throw new BadRequestException("기존 폴더와 새 폴더는 동시에 선택할 수 없습니다.");
        }
        if (!hasFolderId && !hasNewFolderName) {
            return;
        }

        Folder folder = hasFolderId
                ? getOwnedFolder(user.getId(), request.folderId())
                : createFolder(user, request.newFolderName(), request.newFolderColor());
        folderCardRepository.deleteAllByCollectionCard_Id(collectionCard.getId());
        folderCardRepository.flush();
        folderCardRepository.save(new FolderCard(folder, collectionCard, OffsetDateTime.now()));
    }

    private Folder getOwnedFolder(Long userId, Long folderId) {
        return folderRepository.findByIdAndUser_Id(folderId, userId)
                .orElseThrow(() -> new NotFoundException("폴더를 찾을 수 없습니다."));
    }

    private Folder createFolder(User user, String name, String color) {
        String trimmedName = trimRequired(name, "새 폴더 이름은 필수입니다.");
        if (folderRepository.existsByUser_IdAndName(user.getId(), trimmedName)) {
            throw new ConflictException("이미 같은 이름의 폴더가 있습니다.");
        }
        return folderRepository.save(new Folder(user, trimmedName, trimToNull(color)));
    }

    private String trimRequired(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Map<Long, EffectType> getEffectTypesById(List<EffectRequest> effects) {
        Set<Long> effectTypeIds = effects.stream()
                .map(EffectRequest::effectTypeId)
                .collect(Collectors.toSet());
        Map<Long, EffectType> effectTypes = effectTypeRepository.findAllById(effectTypeIds)
                .stream()
                .collect(Collectors.toMap(EffectType::getId, Function.identity()));

        if (effectTypes.size() != effectTypeIds.size()) {
            throw new BadRequestException("존재하지 않는 효과 유형이 포함되어 있습니다.");
        }
        return effectTypes;
    }

    private SurvivalCardResponse toResponse(SurvivalCard card) {
        return new SurvivalCardResponse(
                card.getId(),
                card.getTitle(),
                card.getDescription(),
                card.getRecommendedSituation(),
                card.getDifficulty(),
                card.getImageUrl(),
                card.getStatus(),
                toPrimaryEffectResponse(card.getPrimaryEffectType()),
                toCardEffectResponses(card.getId()),
                card.getCreatedAt()
        );
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
