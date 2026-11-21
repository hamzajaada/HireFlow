package com.hireflow.talents.messaging;

import com.hireflow.talents.config.RabbitConfig;
import com.hireflow.talents.entite.Candidate;
import com.hireflow.talents.entite.ScreeningTest;
import com.hireflow.talents.enums.CandidateStatus;
import com.hireflow.talents.repository.CandidateRepository;
import com.hireflow.talents.repository.ScreeningTestRepository;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.test.TestRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

/**
 * Test d'intégration des 3 listeners SANS serveur RabbitMQ : TestRabbitTemplate (spring-rabbit-test)
 * livre le message directement à nos @RabbitListener, en passant par le vrai convertisseur JSON
 * et la vraie base H2.
 *
 * Les messages sont construits comme ceux de service-candidatures : JSON + en-tête __TypeId__
 * avec le nom de SA classe (package com.hireflow.candidatures...), qui n'existe pas dans talents.
 * Le test prouve que Spring utilise quand même le type du paramètre de nos listeners.
 */
@SpringBootTest
class ListenersIntegrationTest {

    private static final String EMAIL = "karim@email.com";

    @Autowired TestRabbitTemplate rabbit;
    @Autowired CandidateRepository candidates;
    @Autowired ScreeningTestRepository tests;

    @TestConfiguration
    static class RabbitSansServeur {

        /** Fausse connexion : aucun RabbitMQ n'est contacté pendant le test. */
        @Bean
        @Primary
        ConnectionFactory connectionFactory() {
            ConnectionFactory factory = mock(ConnectionFactory.class);
            Connection connection = mock(Connection.class);
            Channel channel = mock(Channel.class);
            willReturn(connection).given(factory).createConnection();
            willReturn(channel).given(connection).createChannel(anyBoolean());
            given(channel.isOpen()).willReturn(true);
            return factory;
        }

        @Bean
        TestRabbitTemplate testRabbitTemplate(ConnectionFactory connectionFactory) {
            return new TestRabbitTemplate(connectionFactory);
        }
    }

    /** Message tel que le publie service-candidatures (Jackson2JsonMessageConverter côté producteur). */
    private static Message messageDeCandidatures(String classeProducteur, String json) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setHeader("__TypeId__", "com.hireflow.candidatures." + classeProducteur);
        return new Message(json.getBytes(StandardCharsets.UTF_8), props);
    }

    private Candidate karim() {
        return candidates.findByEmailIgnoreCase(EMAIL).orElseThrow();
    }

    @Test
    void parcoursComplet_depot_entretien_embauche() {
        // 1. application.submitted -> candidat créé, EN_PROCESSUS (RG5)
        rabbit.send(RabbitConfig.QUEUE_APPLICATION_SUBMITTED, messageDeCandidatures(
                "application.dto.ApplicationSubmittedEvent",
                "{\"applicationId\":101,\"offerId\":1,\"candidatEmail\":\"" + EMAIL + "\","
                        + "\"cvUrl\":\"https://cv.example.com/karim.pdf\",\"date\":\"2026-11-20\"}"));

        assertEquals("karim", karim().getNom());
        assertEquals(CandidateStatus.EN_PROCESSUS, karim().getStatut());

        // 2. interview.completed -> test « Entretien » sur 100, EVALUE (RG6)
        String entretien = "{\"applicationId\":101,\"offerId\":1,\"candidatEmail\":\"" + EMAIL + "\","
                + "\"note\":16,\"avis\":\"Très bon profil\"}";
        rabbit.send(RabbitConfig.QUEUE_INTERVIEW_COMPLETED,
                messageDeCandidatures("interview.dto.InterviewCompletedEvent", entretien));

        assertEquals(CandidateStatus.EVALUE, karim().getStatut());
        List<ScreeningTest> historique = tests.findByCandidateIdOrderByDateDescIdDesc(karim().getId());
        assertEquals(1, historique.size());
        assertEquals("Entretien · candidature 101", historique.get(0).getTest());
        assertEquals(80, historique.get(0).getScore());

        // 3. application.accepted -> EMBAUCHE (RG7)
        String acceptee = "{\"applicationId\":101,\"offerId\":1,\"candidatEmail\":\"" + EMAIL + "\","
                + "\"date\":\"2026-11-20\"}";
        rabbit.send(RabbitConfig.QUEUE_APPLICATION_ACCEPTED,
                messageDeCandidatures("application.dto.ApplicationAcceptedEvent", acceptee));

        assertEquals(CandidateStatus.EMBAUCHE, karim().getStatut());

        // 4. Les mêmes messages reçus une 2e fois ne changent rien (RG8)
        rabbit.send(RabbitConfig.QUEUE_INTERVIEW_COMPLETED,
                messageDeCandidatures("interview.dto.InterviewCompletedEvent", entretien));
        rabbit.send(RabbitConfig.QUEUE_APPLICATION_ACCEPTED,
                messageDeCandidatures("application.dto.ApplicationAcceptedEvent", acceptee));

        assertEquals(1, tests.findByCandidateIdOrderByDateDescIdDesc(karim().getId()).size());
        assertEquals(CandidateStatus.EMBAUCHE, karim().getStatut());
        assertEquals(1, candidates.findAll().stream().filter(c -> c.getEmail().equals(EMAIL)).count());
    }
}
