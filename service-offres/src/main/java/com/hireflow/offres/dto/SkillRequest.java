package com.hireflow.offres.dto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
/** Compétence exigée, envoyée par le RH. Niveau de 1 (débutant) à 5 (expert). */
public record SkillRequest(@NotBlank String nom, @NotNull @Min(1) @Max(5) Integer niveauMinimum) {
}