package com.example.hackathon.domain.draw.entity;

import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import com.example.hackathon.domain.mailing.entity.CardMailing;
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
        name = "mystery_draw_sessions",
        indexes = {
                @Index(name = "idx_mystery_draw_sessions_user_id", columnList = "user_id"),
                @Index(name = "idx_mystery_draw_sessions_status", columnList = "status")
        }
)
public class MysteryDrawSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offered_mailing_id", nullable = false, unique = true)
    private CardMailing offeredMailing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MysteryDrawStatus status = MysteryDrawStatus.OPEN;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    protected MysteryDrawSession() {
    }

    public MysteryDrawSession(User user, CardMailing offeredMailing) {
        this.user = user;
        this.offeredMailing = offeredMailing;
    }

    public void complete(OffsetDateTime completedAt) {
        if (status != MysteryDrawStatus.OPEN) {
            throw new IllegalStateException("선택 가능한 미스터리 뽑기 상태가 아닙니다.");
        }
        this.status = MysteryDrawStatus.COMPLETED;
        this.completedAt = completedAt;
    }

    public void expire() {
        if (status == MysteryDrawStatus.OPEN) {
            this.status = MysteryDrawStatus.EXPIRED;
        }
    }

    public void cancel() {
        if (status == MysteryDrawStatus.COMPLETED) {
            throw new IllegalStateException("완료된 미스터리 뽑기는 취소할 수 없습니다.");
        }
        this.status = MysteryDrawStatus.CANCELED;
    }

    public boolean isOpen() {
        return status == MysteryDrawStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public CardMailing getOfferedMailing() {
        return offeredMailing;
    }

    public MysteryDrawStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }
}
