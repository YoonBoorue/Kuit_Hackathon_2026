package com.example.hackathon.domain.card.repository;

import com.example.hackathon.domain.card.entity.CardEffect;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardEffectRepository extends JpaRepository<CardEffect, Long> {

    List<CardEffect> findAllByCard_IdOrderByDisplayOrderAsc(Long cardId);
}
