package com.example.hackathon.domain.card.repository;

import com.example.hackathon.domain.card.entity.CardStatus;
import com.example.hackathon.domain.card.entity.SurvivalCard;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurvivalCardRepository extends JpaRepository<SurvivalCard, Long> {

    List<SurvivalCard> findAllByAuthorUser_Id(Long authorUserId);

    List<SurvivalCard> findAllByAuthorUser_IdAndStatus(Long authorUserId, CardStatus status);

    long countByAuthorUser_IdAndStatus(Long authorUserId, CardStatus status);
}
