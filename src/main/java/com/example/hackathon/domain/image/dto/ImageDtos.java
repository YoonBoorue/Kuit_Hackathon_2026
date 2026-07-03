package com.example.hackathon.domain.image.dto;

public final class ImageDtos {

    private ImageDtos() {
    }

    public record ImageUploadResponse(
            String imageUrl,
            String imageKey
    ) {
    }
}
