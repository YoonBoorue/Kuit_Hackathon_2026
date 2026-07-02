package com.example.hackathon.domain.user.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateUserRequest(
            @NotBlank(message = "닉네임은 필수입니다.")
            @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
            String nickname
    ) {
    }

    public record UserResponse(
            Long userId,
            String nickname
    ) {
    }

    public record NicknameCheckResponse(
            String nickname,
            boolean available,
            String message
    ) {
    }

    public record UserHomeResponse(
            String nickname,
            long createdCardCount,
            long receivedCardCount,
            long sendableCardCount
    ) {
    }
}
