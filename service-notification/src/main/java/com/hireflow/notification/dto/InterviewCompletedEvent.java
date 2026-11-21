package com.hireflow.notification.dto;

/** Reçu sur interview.completed (publié par service-candidatures). Mêmes champs que le producteur. */
public record InterviewCompletedEvent(Long applicationId, Long offerId, String candidatEmail, Integer note,
                                      String avis) {
}
