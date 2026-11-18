package com.hireflow.candidatures.interview;

import com.hireflow.candidatures.interview.client.TalentClientFallbackFactory;
import org.junit.jupiter.api.Test;
import java.net.ConnectException;
import static org.junit.jupiter.api.Assertions.assertNull;
class RealtimeAndFallbackTest {
    @Test
    void fallbackTalents_renvoieNull() {
        var client = new TalentClientFallbackFactory().create(new ConnectException("Connection refused"));
                assertNull(client.profil("amine@email.com"));
    }
}