package com.example.hackathon.domain.collection.service;

import com.example.hackathon.domain.common.dto.CardDtos.CardEffectResponse;
import com.example.hackathon.domain.common.dto.CardDtos.PrimaryEffectResponse;
import com.example.hackathon.domain.card.entity.CardEffect;
import com.example.hackathon.domain.card.entity.CardStatus;
import com.example.hackathon.domain.card.entity.SurvivalCard;
import com.example.hackathon.domain.card.repository.CardEffectRepository;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.CollectionCardDetailResponse;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.CollectionCardFolderResponse;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.CollectionCardSummaryResponse;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.FavoriteRequest;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.FavoriteResponse;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.FolderTarget;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.MemoRequest;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.MemoResponse;
import com.example.hackathon.domain.collection.dto.CollectionCardDtos.UpdateCollectionCardFolderRequest;
import com.example.hackathon.domain.collection.entity.CollectionCard;
import com.example.hackathon.domain.collection.entity.CollectionSource;
import com.example.hackathon.domain.collection.repository.CollectionCardRepository;
import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.exchange.entity.CardExchange;
import com.example.hackathon.domain.folder.entity.Folder;
import com.example.hackathon.domain.folder.entity.FolderCard;
import com.example.hackathon.domain.folder.repository.FolderCardRepository;
import com.example.hackathon.domain.folder.repository.FolderRepository;
import com.example.hackathon.domain.mailing.entity.CardMailing;
import com.example.hackathon.domain.mailing.repository.CardMailingRepository;
import com.example.hackathon.domain.user.entity.User;
import com.example.hackathon.domain.user.service.UserReader;
import com.example.hackathon.global.dto.MessageResponse;
import com.example.hackathon.global.exception.BadRequestException;
import com.example.hackathon.global.exception.ConflictException;
import com.example.hackathon.global.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CollectionCardService {

    private static final String ALL_FOLDER = "all";
    private static final String DEFAULT_FOLDER = "default";
    private static final String RECEIVED_FOLDER = "received";
    private static final String FAVORITE_FOLDER = "favorite";

    private final CollectionCardRepository collectionCardRepository;
    private final CardEffectRepository cardEffectRepository;
    private final FolderRepository folderRepository;
    private final FolderCardRepository folderCardRepository;
    private final CardMailingRepository cardMailingRepository;
    private final UserReader userReader;

    public CollectionCardService(
            CollectionCardRepository collectionCardRepository,
            CardEffectRepository cardEffectRepository,
            FolderRepository folderRepository,
            FolderCardRepository folderCardRepository,
            CardMailingRepository cardMailingRepository,
            UserReader userReader
    ) {
        this.collectionCardRepository = collectionCardRepository;
        this.cardEffectRepository = cardEffectRepository;
        this.folderRepository = folderRepository;
        this.folderCardRepository = folderCardRepository;
        this.cardMailingRepository = cardMailingRepository;
        this.userReader = userReader;
    }

    public List<CollectionCardSummaryResponse> getCollectionCards(
            Long userId,
            CollectionSource source,
            Boolean favorite,
            String folder,
            Long effectTypeId,
            String effectCode
    ) {
        userReader.getById(userId);
        return findCollectionCards(userId, folder).stream()
                .filter(collectionCard -> matchesFolderFilter(collectionCard, folder))
                .filter(collectionCard -> source == null || collectionCard.getSource() == source)
                .filter(collectionCard -> favorite == null || collectionCard.isFavorite() == favorite)
                .filter(collectionCard -> matchesEffectFilter(collectionCard, effectTypeId, effectCode))
                .map(this::toSummaryResponse)
                .toList();
    }

    public CollectionCardDetailResponse getCollectionCard(Long userId, Long collectionCardId) {
        CollectionCard collectionCard = getOwnedCollectionCard(userId, collectionCardId);
        SurvivalCard card = collectionCard.getCard();
        return new CollectionCardDetailResponse(
                collectionCard.getId(),
                card.getId(),
                collectionCard.getSource(),
                collectionCard.isFavorite(),
                collectionCard.getMemo(),
                card.getTitle(),
                card.getDescription(),
                card.getRecommendedSituation(),
                card.getDifficulty(),
                card.getImageUrl(),
                resolveMessage(collectionCard),
                card.getAuthorUser().getId(),
                card.getAuthorUser().getNickname(),
                toPrimaryEffectResponse(card.getPrimaryEffectType()),
                toCardEffectResponses(card.getId()),
                card.getCreatedAt(),
                collectionCard.getCollectedAt()
        );
    }

    @Transactional
    public FavoriteResponse updateFavorite(Long userId, Long collectionCardId, FavoriteRequest request) {
        CollectionCard collectionCard = getOwnedCollectionCard(userId, collectionCardId);
        if (request.favorite() == null) {
            throw new BadRequestException("즐겨찾기 여부는 필수입니다.");
        }
        collectionCard.changeFavorite(request.favorite());
        return new FavoriteResponse(collectionCard.getId(), collectionCard.isFavorite());
    }

    @Transactional
    public MemoResponse updateMemo(Long userId, Long collectionCardId, MemoRequest request) {
        CollectionCard collectionCard = getOwnedCollectionCard(userId, collectionCardId);
        collectionCard.updateMemo(request.memo());
        return new MemoResponse(collectionCard.getId(), collectionCard.getMemo());
    }

    @Transactional
    public CollectionCardFolderResponse updateFolder(
            Long userId,
            Long collectionCardId,
            UpdateCollectionCardFolderRequest request
    ) {
        CollectionCard collectionCard = getOwnedCollectionCard(userId, collectionCardId);
        if (request.target() == null) {
            throw new BadRequestException("저장 대상은 필수입니다.");
        }

        Folder folder = switch (request.target()) {
            case ALL -> {
                folderCardRepository.deleteAllByCollectionCard_Id(collectionCard.getId());
                yield null;
            }
            case FAVORITE -> {
                collectionCard.changeFavorite(true);
                yield getCurrentFolder(collectionCard);
            }
            case FOLDER -> moveToExistingFolder(userId, collectionCard, request.folderId());
            case NEW_FOLDER -> moveToNewFolder(userId, collectionCard, request.newFolderName(), request.newFolderColor());
        };

        return new CollectionCardFolderResponse(
                collectionCard.getId(),
                folder == null ? null : folder.getId(),
                folder == null ? null : folder.getName(),
                collectionCard.isFavorite(),
                resolveFolderMessage(request.target())
        );
    }

    @Transactional
    public MessageResponse deleteCollectionCard(Long userId, Long collectionCardId) {
        CollectionCard collectionCard = getOwnedCollectionCard(userId, collectionCardId);
        SurvivalCard card = collectionCard.getCard();

        folderCardRepository.deleteAllByCollectionCard_Id(collectionCard.getId());
        collectionCardRepository.delete(collectionCard);

        if (collectionCard.getSource() == CollectionSource.CREATED
                && card.getStatus() == CardStatus.UNSENT
                && card.isWrittenBy(userId)) {
            card.markDeleted();
        }

        return new MessageResponse("보관 카드가 삭제되었습니다.");
    }

    private List<CollectionCard> findCollectionCards(Long userId, String folder) {
        String normalizedFolder = normalizeFolder(folder);
        if (DEFAULT_FOLDER.equals(normalizedFolder)) {
            return collectionCardRepository.findDefaultFolderCardsByUserId(userId);
        }
        return collectionCardRepository.findAllByUser_IdOrderByCollectedAtDescIdDesc(userId);
    }

    private boolean matchesFolderFilter(CollectionCard collectionCard, String folder) {
        String normalizedFolder = normalizeFolder(folder);
        if (normalizedFolder == null || ALL_FOLDER.equals(normalizedFolder) || DEFAULT_FOLDER.equals(normalizedFolder)) {
            return true;
        }
        if (RECEIVED_FOLDER.equals(normalizedFolder)) {
            return collectionCard.getSource() == CollectionSource.RECEIVED;
        }
        if (FAVORITE_FOLDER.equals(normalizedFolder)) {
            return collectionCard.isFavorite();
        }
        throw new BadRequestException("지원하지 않는 보관함 폴더 필터입니다.");
    }

    private String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return null;
        }
        return folder.trim().toLowerCase();
    }

    private boolean matchesEffectFilter(CollectionCard collectionCard, Long effectTypeId, String effectCode) {
        EffectType primaryEffectType = collectionCard.getCard().getPrimaryEffectType();
        boolean matchesId = effectTypeId == null || primaryEffectType.getId().equals(effectTypeId);
        boolean matchesCode = effectCode == null
                || effectCode.isBlank()
                || primaryEffectType.getCode().equalsIgnoreCase(effectCode.trim());
        return matchesId && matchesCode;
    }

    private CollectionCard getOwnedCollectionCard(Long userId, Long collectionCardId) {
        userReader.getById(userId);
        return collectionCardRepository.findByIdAndUser_Id(collectionCardId, userId)
                .orElseThrow(() -> new NotFoundException("보관 카드를 찾을 수 없습니다."));
    }

    private Folder getCurrentFolder(CollectionCard collectionCard) {
        return folderCardRepository.findByCollectionCard_Id(collectionCard.getId())
                .map(FolderCard::getFolder)
                .orElse(null);
    }

    private Folder moveToExistingFolder(Long userId, CollectionCard collectionCard, Long folderId) {
        if (folderId == null) {
            throw new BadRequestException("폴더 ID는 필수입니다.");
        }
        Folder folder = folderRepository.findByIdAndUser_Id(folderId, userId)
                .orElseThrow(() -> new NotFoundException("폴더를 찾을 수 없습니다."));
        moveToFolder(collectionCard, folder);
        return folder;
    }

    private Folder moveToNewFolder(Long userId, CollectionCard collectionCard, String name, String color) {
        User user = userReader.getById(userId);
        String trimmedName = trimRequired(name, "새 폴더 이름은 필수입니다.");
        if (folderRepository.existsByUser_IdAndName(userId, trimmedName)) {
            throw new ConflictException("이미 같은 이름의 폴더가 있습니다.");
        }
        Folder folder = folderRepository.save(new Folder(user, trimmedName, trimToNull(color)));
        moveToFolder(collectionCard, folder);
        return folder;
    }

    private void moveToFolder(CollectionCard collectionCard, Folder folder) {
        folderCardRepository.deleteAllByCollectionCard_Id(collectionCard.getId());
        folderCardRepository.flush();
        folderCardRepository.save(new FolderCard(folder, collectionCard, OffsetDateTime.now()));
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

    private String resolveFolderMessage(FolderTarget target) {
        return switch (target) {
            case ALL -> "전체 보관함에만 보관되도록 변경되었습니다.";
            case FAVORITE -> "즐겨찾기에 추가되었습니다.";
            case FOLDER, NEW_FOLDER -> "폴더가 변경되었습니다.";
        };
    }

    private CollectionCardSummaryResponse toSummaryResponse(CollectionCard collectionCard) {
        SurvivalCard card = collectionCard.getCard();
        return new CollectionCardSummaryResponse(
                collectionCard.getId(),
                card.getId(),
                collectionCard.getSource(),
                collectionCard.isFavorite(),
                card.getTitle(),
                card.getDescription(),
                card.getRecommendedSituation(),
                card.getDifficulty(),
                card.getImageUrl(),
                resolveMessage(collectionCard),
                toPrimaryEffectResponse(card.getPrimaryEffectType()),
                collectionCard.getCollectedAt()
        );
    }

    private String resolveMessage(CollectionCard collectionCard) {
        if (collectionCard.getSource() == CollectionSource.CREATED) {
            return cardMailingRepository.findByCard_Id(collectionCard.getCard().getId())
                    .map(CardMailing::getMessage)
                    .orElse(null);
        }

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
