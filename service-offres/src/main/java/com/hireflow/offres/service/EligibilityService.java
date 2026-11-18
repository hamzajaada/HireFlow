package com.hireflow.offres.service;
import com.hireflow.offres.dto.EligibilityRequest;
import com.hireflow.offres.dto.EligibilityResponse;
import com.hireflow.offres.dto.MissingSkill;
import com.hireflow.offres.entite.JobOffer;
import com.hireflow.offres.entite.RequiredSkill;
import com.hireflow.offres.enums.OfferStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/** RG6 et RG7 : le candidat est éligible si l'offre est OUVERTE et s'il couvre toutes les compétences.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EligibilityService {
    private final JobOfferService jobOfferService;
    public EligibilityResponse verifier(Long offerId, EligibilityRequest req) {
        JobOffer offer = jobOfferService.getEntity(offerId);           // 404 si l'offre n'existe pas
        // niveau déclaré par compétence, sans tenir compte des majuscules ni des espaces
        Map<String, Integer> niveaux = new HashMap<>();
        if (req.competences() != null) {
            req.competences().forEach(c -> niveaux.merge(cle(c.nom()), c.niveau(), Math::max));
        }
        List<MissingSkill> manquantes = offer.getRequiredSkills().stream()
                .filter(s -> niveaux.getOrDefault(cle(s.getNom()), 0) < minimum(s))
                .map(s -> new MissingSkill(s.getNom(), minimum(s), niveaux.get(cle(s.getNom()))))
                .toList();
        boolean ouverte = offer.getStatut() == OfferStatus.OUVERTE;
        boolean couvertes = manquantes.isEmpty();
        return new EligibilityResponse(offer.getId(), offer.getStatut(), ouverte, couvertes, manquantes,
                ouverte && couvertes);
    }
    private static String cle(String nom) {
        return nom == null ? "" : nom.trim().toLowerCase();
    }
    private static int minimum(RequiredSkill s) {
        return s.getNiveauMinimum() == null ? 1 : s.getNiveauMinimum();
    }
}
