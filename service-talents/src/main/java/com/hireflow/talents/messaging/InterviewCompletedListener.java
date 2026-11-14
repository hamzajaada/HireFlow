package com.hireflow.talents.messaging;

import com.hireflow.talents.config.RabbitConfig;
import com.hireflow.talents.dto.InterviewCompletedEvent;
import com.hireflow.talents.service.ScreeningTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewCompletedListener {

    private final ScreeningTestService screeningTestService;

    @RabbitListener(queues = RabbitConfig.QUEUE_INTERVIEW_COMPLETED)
    public void onCompleted(InterviewCompletedEvent e) {
        screeningTestService.enregistrerEntretien(e);                   // statut EVALUE
    }
}
