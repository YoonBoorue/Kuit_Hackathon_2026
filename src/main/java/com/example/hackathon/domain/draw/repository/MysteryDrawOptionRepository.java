package com.example.hackathon.domain.draw.repository;

import com.example.hackathon.domain.draw.entity.MysteryDrawOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MysteryDrawOptionRepository extends JpaRepository<MysteryDrawOption, Long> {

    List<MysteryDrawOption> findAllByMysteryDrawSession_IdOrderByPositionAsc(Long mysteryDrawSessionId);

    Optional<MysteryDrawOption> findByIdAndMysteryDrawSession_Id(Long optionId, Long mysteryDrawSessionId);
}
