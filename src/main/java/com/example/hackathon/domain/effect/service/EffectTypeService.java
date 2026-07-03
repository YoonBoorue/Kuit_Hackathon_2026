package com.example.hackathon.domain.effect.service;

import com.example.hackathon.domain.effect.dto.EffectTypeDtos.EffectTypeResponse;
import com.example.hackathon.domain.effect.entity.EffectType;
import com.example.hackathon.domain.effect.repository.EffectTypeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EffectTypeService {

    private final EffectTypeRepository effectTypeRepository;

    public EffectTypeService(EffectTypeRepository effectTypeRepository) {
        this.effectTypeRepository = effectTypeRepository;
    }

    public List<EffectTypeResponse> getEffectTypes() {
        return effectTypeRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EffectTypeResponse toResponse(EffectType effectType) {
        return new EffectTypeResponse(
                effectType.getId(),
                effectType.getCode(),
                effectType.getName(),
                effectType.getIcon(),
                effectType.getColor(),
                effectType.getDisplayOrder()
        );
    }
}
