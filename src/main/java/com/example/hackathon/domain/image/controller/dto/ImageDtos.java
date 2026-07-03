package com.example.hackathon.domain.image.controller.dto;

public final class ImageDtos {

    private ImageDtos() {
    }

    public record ImageUploadResponse(
            String imageUrl,
            String imageKey
    ) {
    }
}
