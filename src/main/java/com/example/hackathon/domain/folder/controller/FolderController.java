package com.example.hackathon.domain.folder.controller;

import com.example.hackathon.domain.folder.dto.FolderDtos.AddFolderCardRequest;
import com.example.hackathon.domain.folder.dto.FolderDtos.CreateFolderRequest;
import com.example.hackathon.domain.folder.dto.FolderDtos.FolderCardActionResponse;
import com.example.hackathon.domain.folder.dto.FolderDtos.FolderCardSummaryResponse;
import com.example.hackathon.domain.folder.dto.FolderDtos.FolderResponse;
import com.example.hackathon.domain.folder.dto.FolderDtos.UpdateFolderRequest;
import com.example.hackathon.domain.folder.service.FolderService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "폴더", description = "보관 카드를 분류하기 위한 사용자 폴더와 폴더 내 카드를 관리하는 API")
@RestController
@RequestMapping("/api/folders")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @Operation(summary = "폴더 목록 조회", description = "사용자가 직접 만든 폴더 목록과 카드 개수를 조회합니다.")
    @GetMapping
    public List<FolderResponse> getFolders(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId
    ) {
        return folderService.getFolders(userId);
    }

    @Operation(summary = "폴더 생성", description = "보관 카드를 분류할 사용자 폴더를 생성합니다.")
    @PostMapping
    public FolderResponse createFolder(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Valid @RequestBody CreateFolderRequest request
    ) {
        return folderService.createFolder(userId, request);
    }

    @Operation(summary = "폴더 이름/색상 수정", description = "사용자가 만든 폴더의 이름과 색상을 수정합니다.")
    @PatchMapping("/{folderId}")
    public FolderResponse updateFolder(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "폴더 ID", required = true)
            @PathVariable Long folderId,
            @Valid @RequestBody UpdateFolderRequest request
    ) {
        return folderService.updateFolder(userId, folderId, request);
    }

    @Operation(summary = "폴더 삭제", description = "폴더를 삭제합니다. 보관 카드 자체는 삭제하지 않고 폴더 연결만 제거합니다.")
    @DeleteMapping("/{folderId}")
    public MessageResponse deleteFolder(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "폴더 ID", required = true)
            @PathVariable Long folderId
    ) {
        return folderService.deleteFolder(userId, folderId);
    }

    @Operation(summary = "폴더에 카드 추가", description = "보관 카드 하나를 사용자가 만든 폴더에 추가합니다.")
    @PostMapping("/{folderId}/cards")
    public FolderCardActionResponse addCardToFolder(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "폴더 ID", required = true)
            @PathVariable Long folderId,
            @Valid @RequestBody AddFolderCardRequest request
    ) {
        return folderService.addCardToFolder(userId, folderId, request);
    }

    @Operation(summary = "폴더에서 카드 제거", description = "폴더와 보관 카드의 연결을 제거합니다.")
    @DeleteMapping("/{folderId}/cards/{collectionCardId}")
    public MessageResponse removeCardFromFolder(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "폴더 ID", required = true)
            @PathVariable Long folderId,
            @Parameter(description = "보관 카드 ID", required = true)
            @PathVariable Long collectionCardId
    ) {
        return folderService.removeCardFromFolder(userId, folderId, collectionCardId);
    }

    @Operation(summary = "특정 폴더의 카드 목록 조회", description = "사용자가 만든 특정 폴더에 담긴 보관 카드 목록을 조회합니다.")
    @GetMapping("/{folderId}/cards")
    public List<FolderCardSummaryResponse> getFolderCards(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "폴더 ID", required = true)
            @PathVariable Long folderId,
            @Parameter(description = "대표 효과 유형 ID 필터")
            @RequestParam(required = false) Long effectTypeId,
            @Parameter(description = "대표 효과 코드 필터")
            @RequestParam(required = false) String effectCode
    ) {
        return folderService.getFolderCards(userId, folderId, effectTypeId, effectCode);
    }
}
