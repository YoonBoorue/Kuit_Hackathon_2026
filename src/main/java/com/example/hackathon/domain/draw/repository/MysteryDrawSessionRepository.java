package com.example.hackathon.domain.draw.repository;

import com.example.hackathon.domain.draw.entity.MysteryDrawSession;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MysteryDrawSessionRepository extends JpaRepository<MysteryDrawSession, Long> {

    Optional<MysteryDrawSession> findByIdAndUser_Id(Long mysteryDrawSessionId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select session
            from MysteryDrawSession session
            where session.id = :mysteryDrawSessionId
              and session.user.id = :userId
            """)
    Optional<MysteryDrawSession> findByIdAndUserIdForUpdate(
            @Param("mysteryDrawSessionId") Long mysteryDrawSessionId,
            @Param("userId") Long userId
    );
}
