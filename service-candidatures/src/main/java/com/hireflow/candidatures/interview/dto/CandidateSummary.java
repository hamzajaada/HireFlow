package com.hireflow.candidatures.interview.dto;

import com.hireflow.candidatures.interview.client.CandidateProfile;
/** Ce que l'évaluateur voit du candidat au moment de planifier. */
public record CandidateSummary(Integer experienceAnnees, String dernierTest, Integer dernierScore) {
    public static CandidateSummary from(CandidateProfile p) {
        return p == null ? null : new CandidateSummary(p.experienceAnnees(), p.dernierTest(),
                p.dernierScore());
    }
}

