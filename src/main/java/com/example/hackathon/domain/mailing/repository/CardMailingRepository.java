package com.example.hackathon.domain.mailing.repository;

import com.example.hackathon.domain.mailing.entity.CardMailing;
import com.example.hackathon.domain.mailing.entity.MailingStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardMailingRepository extends JpaRepository<CardMailing, Long> {

    boolean existsByCard_Id(Long cardId);

    Optional<CardMailing> findByCard_Id(Long cardId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select mailing
            from CardMailing mailing
            where mailing.id = :mailingId
            """)
    Optional<CardMailing> findByIdForUpdate(@Param("mailingId") Long mailingId);

    List<CardMailing> findAllBySenderUser_Id(Long senderUserId);

    List<CardMailing> findAllBySenderUser_IdAndStatus(Long senderUserId, MailingStatus status);

    @Query("""
            select mailing
            from CardMailing mailing
            where mailing.status = :status
              and mailing.senderUser.id <> :senderUserId
              and mailing.id <> :excludedMailingId
            order by function('random')
            """)
    List<CardMailing> findRandomCandidates(
            @Param("status") MailingStatus status,
            @Param("senderUserId") Long senderUserId,
            @Param("excludedMailingId") Long excludedMailingId,
            Pageable pageable
    );
}
