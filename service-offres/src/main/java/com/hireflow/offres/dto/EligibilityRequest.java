package com.hireflow.offres.dto;
import java.util.List;
/** Corps envoyé par le Feign de Yasmine : POST /offers/{id}/eligibility. */
public record EligibilityRequest(String candidatEmail, List<CandidateSkill> competences) {
}