package com.hireflow.candidatures.application.client;

import java.util.List;

/** Réponse de service-offres (et des fichiers fake). Le statut reste un String : pas besoin de l'enum de Hamza. */
public record EligibilityResponse(Long offerId, String statut, boolean ouverte, boolean competencesCouvertes,
                                  List<MissingSkill> competencesManquantes, boolean eligible) {

    public record MissingSkill(String nom, Integer niveauMinimum, Integer niveauCandidat) {
    }
}
