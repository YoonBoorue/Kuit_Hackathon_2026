package com.example.hackathon.domain.collection.controller;

import com.example.hackathon.domain.collection.controller.dto.CollectionCardDtos.CollectionCardDetailResponse;
import com.example.hackathon.domain.collection.controller.dto.CollectionCardDtos.CollectionCardFolderResponse;
import com.example.hackathon.domain.collection.controller.dto.CollectionCardDtos.CollectionCardSummaryResponse;
import com.example.hackathon.domain.collection.controller.dto.CollectionCardDtos.FavoriteRequest;
import com.example.hackathon.domain.collection.controller.dto.CollectionCardDtos.FavoriteResponse;
import com.example.hackathon.domain.collection.controller.dto.CollectionCardDtos.MemoRequest;
import com.example.hackathon.domain.collection.controller.dto.CollectionCardDtos.MemoResponse;
import com.example.hackathon.domain.collection.controller.dto.CollectionCardDtos.UpdateCollectionCardFolderRequest;
import com.example.hackathon.domain.collection.entity.CollectionSource;
import com.example.hackathon.domain.collection.service.CollectionCardService;
import com.example.hackathon.global.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "보관 카드", description = "사용자가 만들거나 교환으로 받은 보관 카드를 관리하는 API")
@RestController
@RequestMapping("/api/collection-cards")
public class CollectionCardController {

    private final CollectionCardService collectionCardService;

    public CollectionCardController(CollectionCardService collectionCardService) {
        this.collectionCardService = collectionCardService;
    }

    @Operation(summary = "보관함 카드 목록 조회", description = "내 보관 카드를 출처, 즐겨찾기, 가상 폴더 조건으로 조회합니다.")
    @GetMapping
    public List<CollectionCardSummaryResponse> getCollectionCards(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "보관 카드 출처 필터")
            @RequestParam(required = false) CollectionSource source,
            @Parameter(description = "즐겨찾기 여부 필터")
            @RequestParam(required = false) Boolean favorite,
            @Parameter(description = "가상 폴더 필터. 예: default")
            @RequestParam(required = false) String folder,
            @Parameter(description = "대표 효과 유형 ID 필터")
            @RequestParam(required = false) Long effectTypeId,
            @Parameter(description = "대표 효과 코드 필터")
            @RequestParam(required = false) String effectCode
    ) {
        return collectionCardService.getCollectionCards(userId, source, favorite, folder, effectTypeId, effectCode);
    }

    @Operation(summary = "보관 카드 상세 조회", description = "보관 카드의 본문, 대표 효과, 세부 효과, 메모를 조회합니다.")
    @GetMapping("/{collectionCardId}")
    public CollectionCardDetailResponse getCollectionCard(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "보관 카드 ID", required = true)
            @PathVariable Long collectionCardId
    ) {
        return collectionCardService.getCollectionCard(userId, collectionCardId);
    }

    @Operation(summary = "즐겨찾기 변경", description = "보관 카드의 즐겨찾기 여부를 변경합니다.")
    @PatchMapping("/{collectionCardId}/favorite")
    public FavoriteResponse updateFavorite(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "보관 카드 ID", required = true)
            @PathVariable Long collectionCardId,
            @Valid @RequestBody FavoriteRequest request
    ) {
        return collectionCardService.updateFavorite(userId, collectionCardId, request);
    }

    @Operation(summary = "보관 카드 메모 수정", description = "보관 카드에 남긴 개인 메모를 수정합니다.")
    @PatchMapping("/{collectionCardId}/memo")
    public MemoResponse updateMemo(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "보관 카드 ID", required = true)
            @PathVariable Long collectionCardId,
            @Valid @RequestBody MemoRequest request
    ) {
        return collectionCardService.updateMemo(userId, collectionCardId, request);
    }

    @Operation(summary = "보관 카드 저장 대상 변경", description = "이미 저장된 보관 카드를 전체, 즐겨찾기, 기존 폴더, 새 폴더 중 하나로 분류합니다.")
    @PatchMapping("/{collectionCardId}/folder")
    public CollectionCardFolderResponse updateFolder(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "보관 카드 ID", required = true)
            @PathVariable Long collectionCardId,
            @Valid @RequestBody UpdateCollectionCardFolderRequest request
    ) {
        return collectionCardService.updateFolder(userId, collectionCardId, request);
    }

    @Operation(summary = "보관 카드 삭제", description = "내 보관함에서 카드를 삭제합니다. 받은 카드는 보관 카드만 삭제하고, 내가 만든 미발송 카드는 원본 카드도 삭제 상태로 변경합니다.")
    @DeleteMapping("/{collectionCardId}")
    public MessageResponse deleteCollectionCard(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "보관 카드 ID", required = true)
            @PathVariable Long collectionCardId
    ) {
        return collectionCardService.deleteCollectionCard(userId, collectionCardId);
    }
}
