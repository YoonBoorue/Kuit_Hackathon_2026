package com.example.hackathon.domain.effect.controller;

import com.example.hackathon.domain.effect.dto.EffectTypeDtos.EffectTypeResponse;
import com.example.hackathon.domain.effect.service.EffectTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "효과 유형", description = "카드 효과 유형과 표시 정보를 관리하는 API")
@RestController
@RequestMapping("/api/effect-types")
public class EffectTypeController {

    private final EffectTypeService effectTypeService;

    public EffectTypeController(EffectTypeService effectTypeService) {
        this.effectTypeService = effectTypeService;
    }

    @Operation(summary = "효과 종류 조회", description = "카드 작성 화면에서 사용할 효과 종류를 표시 순서대로 조회합니다.")
    @GetMapping
    public List<EffectTypeResponse> getEffectTypes() {
        return effectTypeService.getEffectTypes();
    }
}
