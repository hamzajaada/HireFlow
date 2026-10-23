package com.hireflow.events;

/** Noms des topics Kafka (contrat partage, section 6 du dossier). */
public final class Topics {
    public static final String APPLICATION_SUBMITTED = "application-submitted";
    public static final String INTERVIEW_COMPLETED = "interview-completed";
    public static final String APPLICATION_ACCEPTED = "application-accepted";

    private Topics() {
    }
}
