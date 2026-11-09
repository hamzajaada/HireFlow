package com.hireflow.talents.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.ApplicationRunner;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ cote consommateur. talents a ses propres files, liees a l'exchange de service-candidatures :
 * chaque evenement publie y est copie (un autre service qui ecoute le meme evenement a sa propre file).
 *
 * Reception : @RabbitListener(queues = RabbitConfig.QUEUE_...) sur une methode qui prend le DTO en parametre.
 * Noms partages entre les 3 services : ne pas les changer sans prevenir l'equipe.
 */
@Configuration
public class RabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);

    public static final String EXCHANGE = "hireflow.events";

    public static final String QUEUE_APPLICATION_SUBMITTED = "talents.application-submitted";
    public static final String QUEUE_INTERVIEW_COMPLETED = "talents.interview-completed";
    public static final String QUEUE_APPLICATION_ACCEPTED = "talents.application-accepted";

    public static final String RK_APPLICATION_SUBMITTED = "application.submitted";
    public static final String RK_INTERVIEW_COMPLETED = "interview.completed";
    public static final String RK_APPLICATION_ACCEPTED = "application.accepted";

    @Bean
    public TopicExchange hireflowExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue queueApplicationSubmitted() {
        return new Queue(QUEUE_APPLICATION_SUBMITTED, true);
    }

    @Bean
    public Binding bindingApplicationSubmitted(TopicExchange hireflowExchange) {
        return BindingBuilder.bind(queueApplicationSubmitted()).to(hireflowExchange).with(RK_APPLICATION_SUBMITTED);
    }

    @Bean
    public Queue queueInterviewCompleted() {
        return new Queue(QUEUE_INTERVIEW_COMPLETED, true);
    }

    @Bean
    public Binding bindingInterviewCompleted(TopicExchange hireflowExchange) {
        return BindingBuilder.bind(queueInterviewCompleted()).to(hireflowExchange).with(RK_INTERVIEW_COMPLETED);
    }

    @Bean
    public Queue queueApplicationAccepted() {
        return new Queue(QUEUE_APPLICATION_ACCEPTED, true);
    }

    @Bean
    public Binding bindingApplicationAccepted(TopicExchange hireflowExchange) {
        return BindingBuilder.bind(queueApplicationAccepted()).to(hireflowExchange).with(RK_APPLICATION_ACCEPTED);
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
