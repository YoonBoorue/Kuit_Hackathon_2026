package com.example.hackathon.domain.collection.entity;

import com.example.hackathon.domain.card.entity.SurvivalCard;
import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import com.example.hackathon.domain.exchange.entity.CardExchange;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "collection_cards",
        indexes = {
                @Index(name = "idx_collection_cards_user_id", columnList = "user_id"),
                @Index(name = "idx_collection_cards_card_id", columnList = "card_id"),
                @Index(name = "idx_collection_cards_source", columnList = "source")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_collection_cards_user_card_source",
                        columnNames = {"user_id", "card_id", "source"}
                )
        }
)
public class CollectionCard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private SurvivalCard card;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id")
    private CardExchange exchange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollectionSource source;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;

    @Column(length = 300)
    private String memo;

    @Column(name = "collected_at", nullable = false)
    private OffsetDateTime collectedAt;

    protected CollectionCard() {
    }

    public CollectionCard(
            User user,
            SurvivalCard card,
            CardExchange exchange,
            CollectionSource source,
            OffsetDateTime collectedAt
    ) {
        this.user = user;
        this.card = card;
        this.exchange = exchange;
        this.source = source;
        this.collectedAt = collectedAt;
    }

    public void changeFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public SurvivalCard getCard() {
        return card;
    }

    public CardExchange getExchange() {
        return exchange;
    }

    public CollectionSource getSource() {
        return source;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getMemo() {
        return memo;
    }

    public OffsetDateTime getCollectedAt() {
        return collectedAt;
    }
}
