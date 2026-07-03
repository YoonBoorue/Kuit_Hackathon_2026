package com.example.hackathon.domain.card.controller;

import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.CreateSurvivalCardRequest;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.CreateSurvivalCardResponse;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.SurvivalCardResponse;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.UpdateCardImageRequest;
import com.example.hackathon.domain.card.controller.dto.SurvivalCardDtos.UpdateCardImageResponse;
import com.example.hackathon.domain.card.entity.CardStatus;
import com.example.hackathon.domain.card.service.SurvivalCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "생존 카드", description = "사용자가 작성한 생존 카드와 카드 효과를 관리하는 API")
@RestController
@RequestMapping("/api/survival-cards")
public class SurvivalCardController {

    private final SurvivalCardService survivalCardService;

    public SurvivalCardController(SurvivalCardService survivalCardService) {
        this.survivalCardService = survivalCardService;
    }

    @Operation(summary = "생존법 카드 저장", description = "카드 원본, 카드 효과, 내가 만든 보관 카드를 함께 생성합니다.")
    @PostMapping
    public CreateSurvivalCardResponse createCard(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Valid @RequestBody CreateSurvivalCardRequest request
    ) {
        return survivalCardService.createCard(userId, request);
    }

    @Operation(summary = "내가 만든 카드 목록 조회", description = "내가 작성한 생존 카드를 상태별로 조회합니다.")
    @GetMapping("/me")
    public List<SurvivalCardResponse> getMyCards(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "카드 상태 필터")
            @RequestParam(required = false) CardStatus status
    ) {
        return survivalCardService.getMyCards(userId, status);
    }

    @Operation(summary = "카드 상세 조회", description = "생존 카드의 본문, 대표 효과, 세부 효과를 조회합니다.")
    @GetMapping("/{cardId}")
    public SurvivalCardResponse getCard(
            @Parameter(description = "조회할 생존 카드 ID", required = true)
            @PathVariable Long cardId
    ) {
        return survivalCardService.getCard(cardId);
    }

    @Operation(summary = "카드 이미지 변경", description = "프론트에서 업로드하거나 확보한 이미지 URL을 카드에 매핑합니다. 빈 값이면 카드 이미지 매핑을 제거합니다.")
    @PutMapping("/{cardId}/image")
    public UpdateCardImageResponse updateCardImage(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "이미지를 변경할 생존 카드 ID", required = true)
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardImageRequest request
    ) {
        return survivalCardService.updateCardImage(userId, cardId, request);
    }
}
