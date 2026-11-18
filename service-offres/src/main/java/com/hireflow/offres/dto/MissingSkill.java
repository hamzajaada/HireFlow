package com.hireflow.offres.dto;
/** Une compétence exigée que le candidat n'a pas (niveauCandidat = null) ou pas assez. */
public record MissingSkill(String nom, Integer niveauMinimum, Integer niveauCandidat) {
}