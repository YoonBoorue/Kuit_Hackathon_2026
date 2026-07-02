package com.example.hackathon.domain.exchange.repository;

import com.example.hackathon.domain.exchange.entity.CardExchange;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardExchangeRepository extends JpaRepository<CardExchange, Long> {

    Optional<CardExchange> findByIdAndRequesterUser_Id(Long exchangeId, Long requesterUserId);
}
