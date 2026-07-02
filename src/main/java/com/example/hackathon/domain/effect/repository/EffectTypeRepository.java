package com.example.hackathon.domain.effect.repository;

import com.example.hackathon.domain.effect.entity.EffectType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EffectTypeRepository extends JpaRepository<EffectType, Long> {

    List<EffectType> findAllByOrderByDisplayOrderAsc();
}
