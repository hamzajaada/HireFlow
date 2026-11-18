package com.hireflow.talents.messaging;

import com.hireflow.talents.config.RabbitConfig;
import com.hireflow.talents.dto.ApplicationAcceptedEvent;
import com.hireflow.talents.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationAcceptedListener {

    private final OnboardingService onboardingService;

    @RabbitListener(queues = RabbitConfig.QUEUE_APPLICATION_ACCEPTED)
    public void onAccepted(ApplicationAcceptedEvent e) {
        onboardingService.embaucher(e);                                 // statut EMBAUCHE + email simulé
    }
}
