package com.example.hackathon.domain.mailing.entity;

import com.example.hackathon.domain.card.entity.SurvivalCard;
import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import com.example.hackathon.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "card_mailings",
        indexes = {
                @Index(name = "idx_card_mailings_sender_user_id", columnList = "sender_user_id"),
                @Index(name = "idx_card_mailings_status", columnList = "status")
        }
)
public class CardMailing extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_user_id", nullable = false)
    private User senderUser;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private SurvivalCard card;

    @Column(length = 150)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MailingStatus status = MailingStatus.WAITING;

    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;

    @Column(name = "matched_at")
    private OffsetDateTime matchedAt;

    protected CardMailing() {
    }

    public CardMailing(User senderUser, SurvivalCard card, String message, OffsetDateTime sentAt) {
        this.senderUser = senderUser;
        this.card = card;
        this.message = message;
        this.sentAt = sentAt;
    }

    public void markMatched(OffsetDateTime matchedAt) {
        if (status != MailingStatus.WAITING) {
            throw new IllegalStateException("매칭 가능한 상태의 우편이 아닙니다.");
        }
        this.status = MailingStatus.MATCHED;
        this.matchedAt = matchedAt;
    }

    public void cancel() {
        if (status == MailingStatus.MATCHED) {
            throw new IllegalStateException("이미 매칭된 우편은 취소할 수 없습니다.");
        }
        this.status = MailingStatus.CANCELED;
    }

    public boolean isWaiting() {
        return status == MailingStatus.WAITING;
    }

    public Long getId() {
        return id;
    }

    public User getSenderUser() {
        return senderUser;
    }

    public SurvivalCard getCard() {
        return card;
    }

    public String getMessage() {
        return message;
    }

    public MailingStatus getStatus() {
        return status;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public OffsetDateTime getMatchedAt() {
        return matchedAt;
    }
}
