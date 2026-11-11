package com.hireflow.candidatures.interview.client;

/** Réponse de service-talents (et des fichiers fake) : mêmes noms de champs que chez Douae. */
public record CandidateProfile(Long candidateId, String nom, String email, Integer experienceAnnees,
                               String statut, String dernierTest, Integer dernierScore, Integer
                               nombreTests) {
}


