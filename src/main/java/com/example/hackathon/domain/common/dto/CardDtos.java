package com.example.hackathon.domain.common.dto;

public final class CardDtos {

    private CardDtos() {
    }

    public record PrimaryEffectResponse(
            Long effectTypeId,
            String name,
            String color,
            String icon
    ) {
    }

    public record CardEffectResponse(
            Long effectTypeId,
            String name,
            Short level
    ) {
    }
}
