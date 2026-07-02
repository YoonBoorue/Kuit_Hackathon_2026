package com.example.hackathon.domain.mailing.controller.dto;

import com.example.hackathon.domain.mailing.entity.MailingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public final class CardMailingDtos {

    private CardMailingDtos() {
    }

    public record CreateCardMailingRequest(
            @NotNull(message = "카드 ID는 필수입니다.")
            Long cardId,

            @Size(max = 150, message = "발송 메시지는 150자 이하여야 합니다.")
            String message
    ) {
    }

    public record CardMailingCreateResponse(
            Long mailingId,
            Long cardId,
            MailingStatus status,
            Long mysteryDrawId
    ) {
    }

    public record CardMailingResponse(
            Long mailingId,
            Long cardId,
            String message,
            MailingStatus status,
            OffsetDateTime sentAt,
            OffsetDateTime matchedAt
    ) {
    }
}
