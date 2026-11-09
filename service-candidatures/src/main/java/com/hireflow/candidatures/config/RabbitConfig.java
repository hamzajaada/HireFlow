package com.hireflow.candidatures.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ cote producteur. service-candidatures publie ses evenements sur un exchange "topic" :
 * chaque service consommateur declare sa propre file liee a cet exchange, donc un meme evenement
 * (ex. interview.completed) arrive a la fois a service-offres et a service-talents.
 *
 * Envoi : rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_APPLICATION_SUBMITTED, event);
 * Noms partages entre les 3 services : ne pas les changer sans prevenir l'equipe.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "hireflow.events";

    public static final String RK_APPLICATION_SUBMITTED = "application.submitted";
    public static final String RK_INTERVIEW_COMPLETED = "interview.completed";
    public static final String RK_APPLICATION_ACCEPTED = "application.accepted";

    @Bean
    public TopicExchange hireflowExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
