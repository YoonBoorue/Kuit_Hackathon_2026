package com.example.hackathon.domain.card.entity;

import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "survival_cards")
public class SurvivalCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User authorUser;

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(name = "recommended_situation", nullable = false, length = 20)
    private String recommendedSituation;

    @Column(nullable = false)
    private Short difficulty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_effect_type_id", nullable = false)
    private EffectType primaryEffectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.UNSENT;

    protected SurvivalCard() {
    }

    public SurvivalCard(
            User authorUser,
            String title,
            String description,
            String recommendedSituation,
            Short difficulty,
            EffectType primaryEffectType
    ) {
        this.authorUser = authorUser;
        this.title = title;
        this.description = description;
        this.recommendedSituation = recommendedSituation;
        this.difficulty = difficulty;
        this.primaryEffectType = primaryEffectType;
    }

    public void markSent() {
        if (status != CardStatus.UNSENT) {
            throw new IllegalStateException("발송 가능한 상태의 카드가 아닙니다.");
        }
        this.status = CardStatus.SENT;
    }

    public void markDeleted() {
        this.status = CardStatus.DELETED;
    }

    public boolean isWrittenBy(Long userId) {
        return authorUser != null && authorUser.getId().equals(userId);
    }

    public Long getId() {
        return id;
    }

    public User getAuthorUser() {
        return authorUser;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRecommendedSituation() {
        return recommendedSituation;
    }

    public Short getDifficulty() {
        return difficulty;
    }

    public EffectType getPrimaryEffectType() {
        return primaryEffectType;
    }

    public CardStatus getStatus() {
        return status;
    }
}
