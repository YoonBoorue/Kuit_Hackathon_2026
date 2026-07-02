package com.example.hackathon.domain.folder.controller.dto;

import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.PrimaryEffectResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class FolderDtos {

    private FolderDtos() {
    }

    public record FolderResponse(
            Long folderId,
            String name,
            String color,
            long cardCount
    ) {
    }

    public record CreateFolderRequest(
            @NotBlank(message = "폴더 이름은 필수입니다.")
            @Size(max = 50, message = "폴더 이름은 50자 이하여야 합니다.")
            String name,

            @Size(max = 30, message = "폴더 색상은 30자 이하여야 합니다.")
            String color
    ) {
    }

    public record UpdateFolderRequest(
            @NotBlank(message = "폴더 이름은 필수입니다.")
            @Size(max = 50, message = "폴더 이름은 50자 이하여야 합니다.")
            String name,

            @Size(max = 30, message = "폴더 색상은 30자 이하여야 합니다.")
            String color
    ) {
    }

    public record AddFolderCardRequest(
            @NotNull(message = "보관 카드 ID는 필수입니다.")
            Long collectionCardId
    ) {
    }

    public record FolderCardActionResponse(
            Long folderId,
            Long collectionCardId,
            String message
    ) {
    }

    public record FolderCardSummaryResponse(
            Long collectionCardId,
            Long cardId,
            String title,
            String description,
            PrimaryEffectResponse primaryEffect
    ) {
    }
}
