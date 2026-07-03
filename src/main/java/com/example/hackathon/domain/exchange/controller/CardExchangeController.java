package com.example.hackathon.domain.exchange.controller;

import com.example.hackathon.domain.exchange.dto.CardExchangeDtos.CardExchangeResponse;
import com.example.hackathon.domain.exchange.service.CardExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "카드 교환", description = "미스터리 카드 선택으로 확정된 1:1 카드 교환 기록을 관리하는 API")
@RestController
@RequestMapping("/api/card-exchange")
public class CardExchangeController {

    private final CardExchangeService cardExchangeService;

    public CardExchangeController(CardExchangeService cardExchangeService) {
        this.cardExchangeService = cardExchangeService;
    }

    @Operation(summary = "교환 결과 조회", description = "내가 보낸 카드와 교환으로 받은 카드 정보를 조회합니다.")
    @GetMapping("/{exchangeId}")
    public CardExchangeResponse getExchange(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "카드 교환 ID", required = true)
            @PathVariable Long exchangeId
    ) {
        return cardExchangeService.getExchange(userId, exchangeId);
    }
}
