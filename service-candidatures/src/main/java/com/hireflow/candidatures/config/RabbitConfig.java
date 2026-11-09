package com.hireflow.candidatures.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.ApplicationRunner;
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

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);

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

    /**
     * Cree l'exchange, les files et les liaisons des le demarrage (sinon RabbitMQ ne les cree
     * qu'au premier envoi ou a la premiere ecoute). Si RabbitMQ n'est pas lance, le service
     * demarre quand meme et les cree a la premiere connexion.
     */
    @Bean
    public ApplicationRunner declarerRabbit(AmqpAdmin amqpAdmin) {
        return args -> {
            try {
                if (amqpAdmin instanceof RabbitAdmin rabbitAdmin) {
                    rabbitAdmin.initialize();
                }
            } catch (AmqpException e) {
                log.warn("RabbitMQ injoignable au demarrage ({}) : lancer docker compose up -d", e.getMessage());
            }
        };
    }
}
