package com.example.hackathon.domain.draw.entity;

import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import com.example.hackathon.domain.mailing.entity.CardMailing;
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
        name = "mystery_draw_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_mystery_draw_options_session_candidate",
                        columnNames = {"mystery_draw_session_id", "candidate_mailing_id"}
                ),
                @UniqueConstraint(
                        name = "uk_mystery_draw_options_session_position",
                        columnNames = {"mystery_draw_session_id", "position"}
                )
        }
)
public class MysteryDrawOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mystery_draw_session_id", nullable = false)
    private MysteryDrawSession mysteryDrawSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_mailing_id", nullable = false)
    private CardMailing candidateMailing;

    @Column(nullable = false)
    private Short position;

    protected MysteryDrawOption() {
    }

    public MysteryDrawOption(
            MysteryDrawSession mysteryDrawSession,
            CardMailing candidateMailing,
            Short position
    ) {
        this.mysteryDrawSession = mysteryDrawSession;
        this.candidateMailing = candidateMailing;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public MysteryDrawSession getMysteryDrawSession() {
        return mysteryDrawSession;
    }

    public CardMailing getCandidateMailing() {
        return candidateMailing;
    }

    public Short getPosition() {
        return position;
    }
}
