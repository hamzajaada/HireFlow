package com.hireflow.candidatures.interview.messaging;

import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.config.RabbitConfig;
import com.hireflow.candidatures.interview.dto.InterviewCompletedEvent;
import com.hireflow.candidatures.interview.entite.Interview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/** Publie interview.completed quand un entretien est noté (sprint 2). Hamza et Douae l'écoutent. */
@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewEventProducer {

    private final RabbitTemplate rabbit;

    public void interviewCompleted(Interview i) {
        Application a = i.getApplication();
        var event = new InterviewCompletedEvent(a.getId(), a.getOfferId(), a.getCandidatEmail(), i.getNote(),
                i.getAvis());
        rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_INTERVIEW_COMPLETED, event);
        log.info("Publié {} : entretien {} noté {}/20", RabbitConfig.RK_INTERVIEW_COMPLETED, i.getId(),
                i.getNote());
    }
}
