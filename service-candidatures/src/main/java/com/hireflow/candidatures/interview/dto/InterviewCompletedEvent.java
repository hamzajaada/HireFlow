package com.hireflow.candidatures.interview.dto;

/** Publie quand un entretien est note (routing key interview.completed), consomme par service-offres et service-talents. */
public record InterviewCompletedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        Integer note,
        String avis) {
}
