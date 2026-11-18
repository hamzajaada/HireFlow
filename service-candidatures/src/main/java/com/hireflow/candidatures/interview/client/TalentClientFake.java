package com.hireflow.candidatures.interview.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
/** Sprint 1 : remplace l'appel à service-talents par un fichier JSON (profil "fake"). */
@Component
@Profile("fake")
@RequiredArgsConstructor
public class TalentClientFake implements TalentClient {
    private final ObjectMapper mapper;
    @Override
    public CandidateProfile profil(String email) {
// fake/profile-amine@email.com.json ... ; email inconnu = null, comme le fallback
        ClassPathResource fichier = new ClassPathResource("fake/profile-" + email + ".json");
        if (!fichier.exists()) {
            return null;
        }
        try (InputStream in = fichier.getInputStream()) {
            return mapper.readValue(in, CandidateProfile.class);
        } catch (IOException e) {
            throw new IllegalStateException("Fichier fake illisible : " + fichier, e);
        }
    }
}

