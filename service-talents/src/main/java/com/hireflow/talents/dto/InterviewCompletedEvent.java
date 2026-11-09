package com.hireflow.talents.dto;

/** Recu depuis service-candidatures : publie quand un entretien est note (routing key interview.completed), consomme par service-offres et service-talents. */
public record InterviewCompletedEvent(
        Long applicationId,
        Long offerId,
        String candidatEmail,
        Integer note,
        String avis) {
}
