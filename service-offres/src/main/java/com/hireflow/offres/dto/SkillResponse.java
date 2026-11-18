package com.hireflow.offres.dto;
import com.hireflow.offres.entite.RequiredSkill;
public record SkillResponse(Long id, String nom, Integer niveauMinimum) {
    public static SkillResponse from(RequiredSkill s) {
        return new SkillResponse(s.getId(), s.getNom(), s.getNiveauMinimum());
    }
}