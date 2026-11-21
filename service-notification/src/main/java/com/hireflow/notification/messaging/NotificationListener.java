package com.hireflow.notification.messaging;

import com.hireflow.notification.config.RabbitConfig;
import com.hireflow.notification.dto.ApplicationAcceptedEvent;
import com.hireflow.notification.dto.ApplicationSubmittedEvent;
import com.hireflow.notification.dto.InterviewCompletedEvent;
import com.hireflow.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** Une méthode par file : RabbitMQ livre le message, Spring le convertit du JSON vers le record. */
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService service;

    @RabbitListener(queues = RabbitConfig.QUEUE_APPLICATION_SUBMITTED)
    public void onApplicationSubmitted(ApplicationSubmittedEvent e) {
        service.candidatureRecue(e);
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_INTERVIEW_COMPLETED)
    public void onInterviewCompleted(InterviewCompletedEvent e) {
        service.entretienNote(e);
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_APPLICATION_ACCEPTED)
    public void onApplicationAccepted(ApplicationAcceptedEvent e) {
        service.candidatRetenu(e);
    }
}
