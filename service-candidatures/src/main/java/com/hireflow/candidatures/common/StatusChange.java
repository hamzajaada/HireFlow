package com.hireflow.candidatures.common;

import com.hireflow.candidatures.application.enums.ApplicationStatus;
import java.time.LocalDateTime;
/**
 * « Une candidature a changé de statut ». Publié par ApplicationService (Yasmine) avec
 * ApplicationEventPublisher, écouté par StatusEventPublisher (Abderahman) avec @EventListener.
 * C'est un message Java dans le même programme : ni HTTP, ni RabbitMQ.
 */
public record StatusChange(Long applicationId, ApplicationStatus ancienStatut, ApplicationStatus nouveauStatut,LocalDateTime date) {
}

