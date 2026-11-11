package com.hireflow.candidatures.interview.dto;

import com.hireflow.candidatures.interview.entite.Interview;
import java.time.LocalDate;
public record InterviewResponse(Long id, Long applicationId, LocalDate date, String evaluateur,
                                Integer note, String avis, CandidateSummary candidat) {
    public static InterviewResponse from(Interview i, CandidateSummary candidat) {
        return new InterviewResponse(i.getId(), i.getApplication().getId(), i.getDate(),
                i.getEvaluateur(),
                i.getNote(), i.getAvis(), candidat);
    }
    public static InterviewResponse from(Interview i) {
        return from(i, null);
    }
}
