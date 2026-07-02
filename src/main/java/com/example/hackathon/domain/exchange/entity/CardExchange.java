package com.example.hackathon.domain.exchange.entity;

import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import com.example.hackathon.domain.draw.entity.MysteryDrawSession;
import com.example.hackathon.domain.mailing.entity.CardMailing;
import com.example.hackathon.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "card_exchanges",
        indexes = {
                @Index(name = "idx_card_exchanges_requester_user_id", columnList = "requester_user_id"),
                @Index(name = "idx_card_exchanges_selected_user_id", columnList = "selected_user_id"),
                @Index(name = "idx_card_exchanges_requester_mailing_id", columnList = "requester_mailing_id"),
                @Index(name = "idx_card_exchanges_selected_mailing_id", columnList = "selected_mailing_id")
        }
)
public class CardExchange extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mystery_draw_session_id", nullable = false, unique = true)
    private MysteryDrawSession mysteryDrawSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requesterUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_mailing_id", nullable = false)
    private CardMailing requesterMailing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selected_user_id", nullable = false)
    private User selectedUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selected_mailing_id", nullable = false)
    private CardMailing selectedMailing;

    @Column(name = "exchanged_at", nullable = false)
    private OffsetDateTime exchangedAt;

    protected CardExchange() {
    }

    public CardExchange(
            MysteryDrawSession mysteryDrawSession,
            User requesterUser,
            CardMailing requesterMailing,
            User selectedUser,
            CardMailing selectedMailing,
            OffsetDateTime exchangedAt
    ) {
        this.mysteryDrawSession = mysteryDrawSession;
        this.requesterUser = requesterUser;
        this.requesterMailing = requesterMailing;
        this.selectedUser = selectedUser;
        this.selectedMailing = selectedMailing;
        this.exchangedAt = exchangedAt;
    }

    public Long getId() {
        return id;
    }

    public MysteryDrawSession getMysteryDrawSession() {
        return mysteryDrawSession;
    }

    public User getRequesterUser() {
        return requesterUser;
    }

    public CardMailing getRequesterMailing() {
        return requesterMailing;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public CardMailing getSelectedMailing() {
        return selectedMailing;
    }

    public OffsetDateTime getExchangedAt() {
        return exchangedAt;
    }
}
