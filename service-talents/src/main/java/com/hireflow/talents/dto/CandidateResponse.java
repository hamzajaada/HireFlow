package com.hireflow.talents.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hireflow.talents.entite.Candidate;
import com.hireflow.talents.enums.CandidateStatus;

import java.util.List;

/** tests est absent du JSON dans la liste (null), présent dans le détail. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CandidateResponse(Long id, String nom, String email, String telephone, Integer experienceAnnees,
                                CandidateStatus statut, List<ScreeningTestResponse> tests) {

    public static CandidateResponse court(Candidate c) {
        return new CandidateResponse(c.getId(), c.getNom(), c.getEmail(), c.getTelephone(), c.getExperienceAnnees(),
                c.getStatut(), null);
    }

    public static CandidateResponse detail(Candidate c, List<ScreeningTestResponse> tests) {
        return new CandidateResponse(c.getId(), c.getNom(), c.getEmail(), c.getTelephone(), c.getExperienceAnnees(),
                c.getStatut(), tests);
    }
}
