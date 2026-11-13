package com.hireflow.candidatures.application.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/** Sprint 1 : remplace l'appel à service-offres par un fichier JSON (profil "fake"). */
@Component
@Profile("fake")
@RequiredArgsConstructor
public class OffreClientFake implements OffreClient {

    private final ObjectMapper mapper;

    @Override
    public EligibilityResponse verifier(Long offerId, EligibilityRequest body) {
        // offre 3 = POURVUE dans le data.sql de Hamza : refusée ; les autres : acceptées
        String fichier = offerId == 3 ? "fake/eligibility-refusee.json" : "fake/eligibility-ok.json";
        try (InputStream in = new ClassPathResource(fichier).getInputStream()) {
            return mapper.readValue(in, EligibilityResponse.class);
        } catch (IOException e) {
            throw new IllegalStateException("Fichier fake illisible : " + fichier, e);
        }
    }
}
