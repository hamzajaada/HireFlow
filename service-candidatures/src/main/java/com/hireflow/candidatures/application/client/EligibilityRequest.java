package com.hireflow.candidatures.application.client;

import com.hireflow.candidatures.application.dto.DeclaredSkill;

import java.util.List;

/** Corps envoyé à service-offres : mêmes noms de champs que chez Hamza. */
public record EligibilityRequest(String candidatEmail, List<DeclaredSkill> competences) {
}
