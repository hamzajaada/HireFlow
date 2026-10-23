package com.hireflow.events;

/** Publie quand un entretien est note, consomme par service-offres et service-talents. */
public record InterviewCompletedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        Integer note,
        String avis) {
}
