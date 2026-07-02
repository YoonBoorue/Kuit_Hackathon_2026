package com.example.hackathon.domain.effect.controller.dto;

public final class EffectTypeDtos {

    private EffectTypeDtos() {
    }

    public record EffectTypeResponse(
            Long effectTypeId,
            String code,
            String name,
            String icon,
            String color,
            Short displayOrder
    ) {
    }
}
