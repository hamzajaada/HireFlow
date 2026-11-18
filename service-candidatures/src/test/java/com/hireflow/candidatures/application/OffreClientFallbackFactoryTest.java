package com.hireflow.candidatures.application;

import com.hireflow.candidatures.application.client.OffreClientFallbackFactory;
import com.hireflow.candidatures.common.ResourceNotFoundException;
import com.hireflow.candidatures.common.ServiceUnavailableException;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OffreClientFallbackFactoryTest {

    private final OffreClientFallbackFactory factory = new OffreClientFallbackFactory();

    @Test
    void offreInconnue_donne404() {
        Request r = Request.create(Request.HttpMethod.POST, "/offers/99/eligibility", Map.of(), null,
                StandardCharsets.UTF_8, null);
        var notFound = new FeignException.NotFound("404", r, null, Map.of());

        assertThrows(ResourceNotFoundException.class, () -> factory.create(notFound).verifier(99L, null));
    }

    @Test
    void serviceArrete_donne503() {
        assertThrows(ServiceUnavailableException.class,
                () -> factory.create(new ConnectException("Connection refused")).verifier(1L, null));
    }
}
