package com.hireflow.talents.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** Sans RabbitMQ : on vérifie seulement où part un message en échec et ce qu'il emporte. */
class RabbitConfigDeadLetterTest {

    private final RabbitConfig config = new RabbitConfig();

    @Test
    void dlq_estLieeAuDeadLetterExchange() {
        Binding binding = config.bindingDeadLetter();

        assertEquals(RabbitConfig.DLX, binding.getExchange());
        assertEquals(RabbitConfig.DLQ, binding.getDestination());
    }

    @Test
    void messageEnEchec_republieDansLaDlqAvecLaCause() {
        AmqpTemplate template = mock(AmqpTemplate.class);
        MessageRecoverer recoverer = config.messageRecoverer(template);

        MessageProperties props = new MessageProperties();
        props.setReceivedExchange(RabbitConfig.EXCHANGE);
        props.setReceivedRoutingKey(RabbitConfig.RK_APPLICATION_SUBMITTED);
        Message poison = new Message("{\"candidatEmail\":\"nadia@\"}".getBytes(StandardCharsets.UTF_8), props);

        recoverer.recover(poison, new IllegalArgumentException("Email invalide reçu par RabbitMQ : nadia@"));

        ArgumentCaptor<Message> envoye = ArgumentCaptor.forClass(Message.class);
        verify(template).send(eq(RabbitConfig.DLX), anyString(), envoye.capture());
        var headers = envoye.getValue().getMessageProperties().getHeaders();
        assertTrue(headers.get("x-exception-message").toString().contains("nadia@"));
        assertEquals(RabbitConfig.RK_APPLICATION_SUBMITTED, headers.get("x-original-routingKey"));
        assertEquals("{\"candidatEmail\":\"nadia@\"}", new String(envoye.getValue().getBody(), StandardCharsets.UTF_8));
    }
}
