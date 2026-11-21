package com.hireflow.notification.enums;

/** Un type de notification par événement RabbitMQ. */
public enum TypeNotification {
    CANDIDATURE_RECUE,   // application.submitted
    ENTRETIEN_NOTE,      // interview.completed
    CANDIDAT_RETENU      // application.accepted
}
