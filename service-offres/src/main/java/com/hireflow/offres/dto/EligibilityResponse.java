package com.hireflow.offres.dto;
import com.hireflow.offres.enums.OfferStatus;
import java.util.List;
/** Réponse lue par Yasmine. Ne pas renommer un champ sans la prévenir (son fake JSON a les mêmes noms).
 */
public record EligibilityResponse(Long offerId, OfferStatus statut, boolean ouverte, boolean
competencesCouvertes,
                                  List<MissingSkill> competencesManquantes, boolean eligible) {
}