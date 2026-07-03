package com.example.hackathon.domain.mailing.controller;

import com.example.hackathon.domain.mailing.dto.CardMailingDtos.CardMailingCreateResponse;
import com.example.hackathon.domain.mailing.dto.CardMailingDtos.CardMailingResponse;
import com.example.hackathon.domain.mailing.dto.CardMailingDtos.CreateCardMailingRequest;
import com.example.hackathon.domain.mailing.entity.MailingStatus;
import com.example.hackathon.domain.mailing.service.CardMailingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "카드 발송", description = "생존 카드를 메시지와 함께 발송하고 매칭 상태를 관리하는 API")
@RestController
@RequestMapping("/api/card-mailings")
public class CardMailingController {

    private final CardMailingService cardMailingService;

    public CardMailingController(CardMailingService cardMailingService) {
        this.cardMailingService = cardMailingService;
    }

    @Operation(summary = "카드 발송하기", description = "생존 카드를 서버 교환 풀에 우편화하고 미스터리 후보 4개를 생성합니다.")
    @PostMapping
    public CardMailingCreateResponse createMailing(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Valid @RequestBody CreateCardMailingRequest request
    ) {
        return cardMailingService.createMailing(userId, request);
    }

    @Operation(summary = "내 발송 우편 조회", description = "내가 발송한 우편화된 카드 목록을 상태별로 조회합니다.")
    @GetMapping("/me")
    public List<CardMailingResponse> getMyMailings(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "우편 상태 필터")
            @RequestParam(required = false) MailingStatus status
    ) {
        return cardMailingService.getMyMailings(userId, status);
    }
}
