package com.hireflow.candidatures.interview;

import com.hireflow.candidatures.application.enums.ApplicationStatus;
import com.hireflow.candidatures.common.StatusChange;
import com.hireflow.candidatures.interview.client.TalentClientFallbackFactory;
import com.hireflow.candidatures.realtime.StatusEventPublisher;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RealtimeAndFallbackTest {

    @Test
    void fallbackTalents_renvoieNull() {
        var client = new TalentClientFallbackFactory().create(new ConnectException("Connection refused"));
        assertNull(client.profil("amine@email.com"));
    }

    @Test
    void unChangementDeStatut_estEnvoyeAuxEcransConnectes() {
        StatusEventPublisher publisher = new StatusEventPublisher();
        publisher.subscribe();

        assertEquals(1, publisher.connectedClients());
        assertDoesNotThrow(() -> publisher.onStatusChange(new StatusChange(12L,
                ApplicationStatus.DEPOSEE,
                ApplicationStatus.EN_ENTRETIEN, LocalDateTime.now())));
    }
}
