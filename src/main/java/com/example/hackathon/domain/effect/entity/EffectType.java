package com.example.hackathon.domain.effect.entity;

import com.example.hackathon.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "effect_types")
public class EffectType extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    @Column(length = 100)
    private String icon;

    @Column(nullable = false, length = 30)
    private String color;

    @Column(name = "display_order", nullable = false)
    private Short displayOrder;

    protected EffectType() {
    }

    public EffectType(String code, String name, String icon, String color, Short displayOrder) {
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public Short getDisplayOrder() {
        return displayOrder;
    }
}
