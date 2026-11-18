package com.hireflow.talents.dto;

import com.hireflow.talents.enums.CandidateStatus;

/** Contrat Feign ② : mêmes noms de champs que CandidateProfile chez Abderahman (et que son fake JSON). */
public record CandidateProfileResponse(Long candidateId, String nom, String email, Integer experienceAnnees,
                                       CandidateStatus statut, String dernierTest, Integer dernierScore,
                                       int nombreTests) {
}
