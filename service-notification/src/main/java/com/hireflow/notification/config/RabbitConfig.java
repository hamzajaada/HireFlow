package com.hireflow.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ côté consommateur. Le service a SES PROPRES files (notification.*), liées au même exchange
 * que les autres : chaque événement arrive donc aussi chez lui, sans que les producteurs le sachent.
 * Noms de l'exchange et des routing keys partagés entre les services : ne pas les changer.
 */
@Configuration
public class RabbitConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitConfig.class);

    public static final String EXCHANGE = "hireflow.events";

    public static final String RK_APPLICATION_SUBMITTED = "application.submitted";
    public static final String RK_INTERVIEW_COMPLETED = "interview.completed";
    public static final String RK_APPLICATION_ACCEPTED = "application.accepted";

    public static final String QUEUE_APPLICATION_SUBMITTED = "notification.application-submitted";
    public static final String QUEUE_INTERVIEW_COMPLETED = "notification.interview-completed";
    public static final String QUEUE_APPLICATION_ACCEPTED = "notification.application-accepted";

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

    /** Crée l'exchange, les files et les liaisons dès le démarrage (comme dans les autres services). */
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
