package com.example.hackathon.domain.collection.controller.dto;

import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.CardEffectResponse;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.PrimaryEffectResponse;
import com.example.hackathon.domain.collection.entity.CollectionSource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public final class CollectionCardDtos {

    private CollectionCardDtos() {
    }

    public record CollectionCardSummaryResponse(
            Long collectionCardId,
            Long cardId,
            CollectionSource source,
            boolean favorite,
            String title,
            String description,
            String recommendedSituation,
            Short difficulty,
            String message,
            PrimaryEffectResponse primaryEffect,
            OffsetDateTime collectedAt
    ) {
    }

    public record CollectionCardDetailResponse(
            Long collectionCardId,
            Long cardId,
            CollectionSource source,
            boolean favorite,
            String memo,
            String title,
            String description,
            String recommendedSituation,
            Short difficulty,
            String message,
            Long authorUserId,
            String authorNickname,
            PrimaryEffectResponse primaryEffect,
            List<CardEffectResponse> effects,
            LocalDateTime cardCreatedAt,
            OffsetDateTime collectedAt
    ) {
    }

    public record FavoriteRequest(
            @NotNull(message = "즐겨찾기 여부는 필수입니다.")
            Boolean favorite
    ) {
    }

    public record FavoriteResponse(
            Long collectionCardId,
            boolean favorite
    ) {
    }

    public record MemoRequest(
            @Size(max = 300, message = "메모는 300자 이하여야 합니다.")
            String memo
    ) {
    }

    public record MemoResponse(
            Long collectionCardId,
            String memo
    ) {
    }

    public enum FolderTarget {
        ALL,
        FAVORITE,
        FOLDER,
        NEW_FOLDER
    }

    public record UpdateCollectionCardFolderRequest(
            @NotNull(message = "저장 대상은 필수입니다.")
            FolderTarget target,

            Long folderId,

            @Size(max = 50, message = "새 폴더 이름은 50자 이하여야 합니다.")
            String newFolderName,

            @Size(max = 30, message = "새 폴더 색상은 30자 이하여야 합니다.")
            String newFolderColor
    ) {
    }

    public record CollectionCardFolderResponse(
            Long collectionCardId,
            Long folderId,
            String folderName,
            boolean favorite,
            String message
    ) {
    }
}
