package com.example.hackathon.domain.card.entity;

import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import com.example.hackathon.domain.effect.entity.EffectType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "card_effects",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_card_effects_card_effect_type", columnNames = {"card_id", "effect_type_id"}),
                @UniqueConstraint(name = "uk_card_effects_card_display_order", columnNames = {"card_id", "display_order"})
        }
)
public class CardEffect extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private SurvivalCard card;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "effect_type_id", nullable = false)
    private EffectType effectType;

    @Column(nullable = false)
    private Short level;

    @Column(name = "display_order", nullable = false)
    private Short displayOrder;

    protected CardEffect() {
    }

    public CardEffect(SurvivalCard card, EffectType effectType, Short level, Short displayOrder) {
        this.card = card;
        this.effectType = effectType;
        this.level = level;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public SurvivalCard getCard() {
        return card;
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public Short getLevel() {
        return level;
    }

    public Short getDisplayOrder() {
        return displayOrder;
    }
}
