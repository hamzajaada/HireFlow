package com.hireflow.candidatures.application.messaging;

import com.hireflow.candidatures.application.dto.ApplicationAcceptedEvent;
import com.hireflow.candidatures.application.dto.ApplicationSubmittedEvent;
import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;

/**
 * Publie les 2 événements de Yasmine sur l'exchange hireflow.events.
 * La publication a lieu APRÈS le commit de la transaction (comme dans le TP) : si l'enregistrement
 * en base échoue, aucun message ne part, donc aucun email n'annonce une candidature inexistante.
 * Si RabbitMQ est injoignable, la candidature reste enregistrée : l'erreur est seulement journalisée.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventProducer {

    private final RabbitTemplate rabbit;

    public void applicationSubmitted(Application a) {
        var event = new ApplicationSubmittedEvent(a.getId(), a.getOfferId(), a.getCandidatEmail(), a.getCvUrl(),
                a.getDate());
        apresCommit(RabbitConfig.RK_APPLICATION_SUBMITTED, event, a.getId());
    }

    public void applicationAccepted(Application a) {
        var event = new ApplicationAcceptedEvent(a.getId(), a.getOfferId(), a.getCandidatEmail(), LocalDate.now());
        apresCommit(RabbitConfig.RK_APPLICATION_ACCEPTED, event, a.getId());
    }

    private void apresCommit(String routingKey, Object event, Long applicationId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publier(routingKey, event, applicationId);
                }
            });
        } else {
            publier(routingKey, event, applicationId);      // appelé hors transaction : on publie tout de suite
        }
    }

    private void publier(String routingKey, Object event, Long applicationId) {
        try {
            rabbit.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
            log.info("Publié {} : candidature {}", routingKey, applicationId);
        } catch (AmqpException e) {
            log.error("Candidature {} enregistrée, mais publication de {} en échec", applicationId, routingKey, e);
        }
    }
}
