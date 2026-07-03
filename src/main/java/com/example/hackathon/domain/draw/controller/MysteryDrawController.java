package com.example.hackathon.domain.draw.controller;

import com.example.hackathon.domain.draw.dto.MysteryDrawDtos.MysteryDrawResponse;
import com.example.hackathon.domain.draw.dto.MysteryDrawDtos.SelectMysteryDrawRequest;
import com.example.hackathon.domain.draw.dto.MysteryDrawDtos.SelectMysteryDrawResponse;
import com.example.hackathon.domain.draw.service.MysteryDrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "미스터리 뽑기", description = "발송 후 제공되는 미스터리 카드 선택 세션과 후보를 관리하는 API")
@RestController
@RequestMapping("/api/mystery-draws")
public class MysteryDrawController {

    private final MysteryDrawService mysteryDrawService;

    public MysteryDrawController(MysteryDrawService mysteryDrawService) {
        this.mysteryDrawService = mysteryDrawService;
    }

    @Operation(summary = "미스터리 뽑기 조회", description = "카드 상세는 숨기고 후보의 메시지와 대표 효과만 조회합니다.")
    @GetMapping("/{mysteryDrawId}")
    public MysteryDrawResponse getMysteryDraw(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "미스터리 뽑기 세션 ID", required = true)
            @PathVariable Long mysteryDrawId
    ) {
        return mysteryDrawService.getMysteryDraw(userId, mysteryDrawId);
    }

    @Operation(summary = "미스터리 카드 선택", description = "후보 하나를 선택해 1:1 카드 교환을 확정하고 받은 카드를 보관함에 저장합니다.")
    @PostMapping("/{mysteryDrawId}/select")
    public SelectMysteryDrawResponse selectMysteryCard(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "미스터리 뽑기 세션 ID", required = true)
            @PathVariable Long mysteryDrawId,
            @Valid @RequestBody SelectMysteryDrawRequest request
    ) {
        return mysteryDrawService.selectMysteryCard(userId, mysteryDrawId, request);
    }
}
