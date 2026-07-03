package com.example.hackathon.domain.folder.service;

import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.PrimaryEffectResponse;
import com.example.hackathon.domain.card.entity.SurvivalCard;
import com.example.hackathon.domain.collection.entity.CollectionCard;
import com.example.hackathon.domain.collection.repository.CollectionCardRepository;
import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.folder.controller.dto.FolderDtos.AddFolderCardRequest;
import com.example.hackathon.domain.folder.controller.dto.FolderDtos.CreateFolderRequest;
import com.example.hackathon.domain.folder.controller.dto.FolderDtos.FolderCardActionResponse;
import com.example.hackathon.domain.folder.controller.dto.FolderDtos.FolderCardSummaryResponse;
import com.example.hackathon.domain.folder.controller.dto.FolderDtos.FolderResponse;
import com.example.hackathon.domain.folder.controller.dto.FolderDtos.UpdateFolderRequest;
import com.example.hackathon.domain.folder.entity.Folder;
import com.example.hackathon.domain.folder.entity.FolderCard;
import com.example.hackathon.domain.folder.repository.FolderCardRepository;
import com.example.hackathon.domain.folder.repository.FolderRepository;
import com.example.hackathon.domain.user.entity.User;
import com.example.hackathon.domain.user.service.UserReader;
import com.example.hackathon.global.dto.MessageResponse;
import com.example.hackathon.global.exception.BadRequestException;
import com.example.hackathon.global.exception.ConflictException;
import com.example.hackathon.global.exception.ForbiddenException;
import com.example.hackathon.global.exception.NotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FolderService {

    private final FolderRepository folderRepository;
    private final FolderCardRepository folderCardRepository;
    private final CollectionCardRepository collectionCardRepository;
    private final UserReader userReader;

    public FolderService(
            FolderRepository folderRepository,
            FolderCardRepository folderCardRepository,
            CollectionCardRepository collectionCardRepository,
            UserReader userReader
    ) {
        this.folderRepository = folderRepository;
        this.folderCardRepository = folderCardRepository;
        this.collectionCardRepository = collectionCardRepository;
        this.userReader = userReader;
    }

    public List<FolderResponse> getFolders(Long userId) {
        userReader.getById(userId);
        return folderRepository.findAllByUser_Id(userId)
                .stream()
                .map(this::toFolderResponse)
                .toList();
    }

    @Transactional
    public FolderResponse createFolder(Long userId, CreateFolderRequest request) {
        User user = userReader.getById(userId);
        String name = validateAndTrimName(request.name());
        String color = trimToNull(request.color());
        validateUniqueFolderName(userId, name);

        Folder folder = folderRepository.save(new Folder(user, name, color));
        return toFolderResponse(folder);
    }

    @Transactional
    public FolderResponse updateFolder(Long userId, Long folderId, UpdateFolderRequest request) {
        Folder folder = getOwnedFolder(userId, folderId);
        String name = validateAndTrimName(request.name());
        String color = trimToNull(request.color());

        if (folderRepository.existsByUser_IdAndNameAndIdNot(userId, name, folderId)) {
            throw new ConflictException("이미 같은 이름의 폴더가 있습니다.");
        }

        folder.update(name, color);
        return toFolderResponse(folder);
    }

    @Transactional
    public MessageResponse deleteFolder(Long userId, Long folderId) {
        Folder folder = getOwnedFolder(userId, folderId);
        folderCardRepository.deleteAllByFolder_Id(folder.getId());
        folderRepository.delete(folder);
        return new MessageResponse("폴더가 삭제되었습니다.");
    }

    @Transactional
    public FolderCardActionResponse addCardToFolder(Long userId, Long folderId, AddFolderCardRequest request) {
        Folder folder = getOwnedFolder(userId, folderId);
        CollectionCard collectionCard = getOwnedCollectionCard(userId, request.collectionCardId());

        FolderCard existingFolderCard = folderCardRepository.findByCollectionCard_Id(collectionCard.getId())
                .orElse(null);
        if (existingFolderCard != null && existingFolderCard.getFolder().getId().equals(folderId)) {
            return new FolderCardActionResponse(
                    folder.getId(),
                    collectionCard.getId(),
                    "이미 해당 폴더에 지정된 카드입니다."
            );
        }

        folderCardRepository.deleteAllByCollectionCard_Id(collectionCard.getId());
        folderCardRepository.flush();
        folderCardRepository.save(new FolderCard(folder, collectionCard, OffsetDateTime.now()));
        return new FolderCardActionResponse(
                folder.getId(),
                collectionCard.getId(),
                "카드 폴더가 변경되었습니다."
        );
    }

    @Transactional
    public MessageResponse removeCardFromFolder(Long userId, Long folderId, Long collectionCardId) {
        getOwnedFolder(userId, folderId);
        FolderCard folderCard = folderCardRepository.findByFolder_IdAndCollectionCard_Id(folderId, collectionCardId)
                .orElseThrow(() -> new NotFoundException("폴더에 담긴 보관 카드를 찾을 수 없습니다."));

        if (!folderCard.getCollectionCard().getUser().getId().equals(userId)) {
            throw new ForbiddenException("내 보관 카드만 폴더에서 제거할 수 있습니다.");
        }

        folderCardRepository.delete(folderCard);
        return new MessageResponse("폴더에서 카드가 제거되었습니다.");
    }

    public List<FolderCardSummaryResponse> getFolderCards(
            Long userId,
            Long folderId,
            Long effectTypeId,
            String effectCode
    ) {
        getOwnedFolder(userId, folderId);
        return folderCardRepository.findAllByFolder_IdOrderByAddedAtDescIdDesc(folderId)
                .stream()
                .filter(folderCard -> folderCard.getCollectionCard().getUser().getId().equals(userId))
                .filter(folderCard -> matchesEffectFilter(folderCard.getCollectionCard(), effectTypeId, effectCode))
                .map(this::toFolderCardSummaryResponse)
                .toList();
    }

    private Folder getOwnedFolder(Long userId, Long folderId) {
        userReader.getById(userId);
        return folderRepository.findByIdAndUser_Id(folderId, userId)
                .orElseThrow(() -> new NotFoundException("폴더를 찾을 수 없습니다."));
    }

    private CollectionCard getOwnedCollectionCard(Long userId, Long collectionCardId) {
        if (collectionCardId == null) {
            throw new BadRequestException("보관 카드 ID는 필수입니다.");
        }
        return collectionCardRepository.findByIdAndUser_Id(collectionCardId, userId)
                .orElseThrow(() -> new NotFoundException("보관 카드를 찾을 수 없습니다."));
    }

    private void validateUniqueFolderName(Long userId, String name) {
        if (folderRepository.existsByUser_IdAndName(userId, name)) {
            throw new ConflictException("이미 같은 이름의 폴더가 있습니다.");
        }
    }

    private String validateAndTrimName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("폴더 이름은 필수입니다.");
        }
        return name.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private FolderResponse toFolderResponse(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getColor(),
                folderCardRepository.countByFolder_Id(folder.getId())
        );
    }

    private FolderCardSummaryResponse toFolderCardSummaryResponse(FolderCard folderCard) {
        CollectionCard collectionCard = folderCard.getCollectionCard();
        SurvivalCard card = collectionCard.getCard();
        return new FolderCardSummaryResponse(
                collectionCard.getId(),
                card.getId(),
                card.getTitle(),
                card.getDescription(),
                card.getImageUrl(),
                toPrimaryEffectResponse(card.getPrimaryEffectType())
        );
    }

    private boolean matchesEffectFilter(CollectionCard collectionCard, Long effectTypeId, String effectCode) {
        EffectType primaryEffectType = collectionCard.getCard().getPrimaryEffectType();
        boolean matchesId = effectTypeId == null || primaryEffectType.getId().equals(effectTypeId);
        boolean matchesCode = effectCode == null
                || effectCode.isBlank()
                || primaryEffectType.getCode().equalsIgnoreCase(effectCode.trim());
        return matchesId && matchesCode;
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
