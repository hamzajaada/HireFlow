package com.hireflow.candidatures.application.messaging;

import com.hireflow.candidatures.application.dto.ApplicationAcceptedEvent;
import com.hireflow.candidatures.application.dto.ApplicationSubmittedEvent;
import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Publie les 2 événements de Yasmine sur l'exchange hireflow.events (sprint 2). */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationEventProducer {

    private final RabbitTemplate rabbit;

    public void applicationSubmitted(Application a) {
        var event = new ApplicationSubmittedEvent(a.getId(), a.getOfferId(), a.getCandidatEmail(), a.getCvUrl(),
                a.getDate());
        rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_APPLICATION_SUBMITTED, event);
        log.info("Publié {} : candidature {}", RabbitConfig.RK_APPLICATION_SUBMITTED, a.getId());
    }

    public void applicationAccepted(Application a) {
        var event = new ApplicationAcceptedEvent(a.getId(), a.getOfferId(), a.getCandidatEmail(), LocalDate.now());
        rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_APPLICATION_ACCEPTED, event);
        log.info("Publié {} : candidature {}", RabbitConfig.RK_APPLICATION_ACCEPTED, a.getId());
    }
}
