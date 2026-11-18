package com.hireflow.offres.service;
import com.hireflow.offres.dto.JobOfferRequest;
import com.hireflow.offres.dto.JobOfferResponse;
import com.hireflow.offres.dto.SkillRequest;
import com.hireflow.offres.dto.SkillResponse;
import com.hireflow.offres.entite.JobOffer;
import com.hireflow.offres.entite.RequiredSkill;
import com.hireflow.offres.enums.OfferStatus;
import com.hireflow.offres.exception.BusinessException;
import com.hireflow.offres.exception.ResourceNotFoundException;
import com.hireflow.offres.repository.JobOfferRepository;
import com.hireflow.offres.repository.RequiredSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
/** Toutes les règles des offres (RG1 à RG5, RG8 à RG10). Le contrôleur ne fait que déléguer ici. */
@Service
@RequiredArgsConstructor
@Transactional
public class JobOfferService {
    private final JobOfferRepository offers;
    private final RequiredSkillRepository skills;
    /** RG1 : une offre est créée OUVERTE. */
    public JobOfferResponse create(JobOfferRequest req) {
        JobOffer offer = JobOffer.builder()
                .intitule(req.intitule())
                .departement(req.departement())
                .salaireMax(req.salaireMax())
                .datePublication(req.datePublication() != null ? req.datePublication() :
                        LocalDate.now())
                .build();                                   // statut = OUVERTE par défau (@Builder.Default)
        if (req.requiredSkills() != null) {
            req.requiredSkills().forEach(s -> offer.getRequiredSkills().add(toSkill(s, offer)));
        }
        return JobOfferResponse.from(offers.save(offer));   // cascade = ALL enregistre aussi les compétences
    }
    @Transactional(readOnly = true)
    public List<JobOfferResponse> findAll(OfferStatus statut) {
        List<JobOffer> list = statut == null ? offers.findAll() : offers.findByStatut(statut);
        return list.stream().map(JobOfferResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public JobOfferResponse findById(Long id) {
        return JobOfferResponse.from(getEntity(id));
    }
    /** RG5 : une offre POURVUE ne se modifie plus. */
    public JobOfferResponse update(Long id, JobOfferRequest req) {
        JobOffer offer = getEntity(id);
        if (offer.getStatut() == OfferStatus.POURVUE) {
            throw new BusinessException("L'offre " + id + " est POURVUE, elle ne peut plus être modifiée");
        }
        offer.setIntitule(req.intitule());
        offer.setDepartement(req.departement());
        offer.setSalaireMax(req.salaireMax());
        return JobOfferResponse.from(offer);                // pas besoin de save : JPA suit l'entité
    }
    public void delete(Long id) {
        offers.delete(getEntity(id));
    }
    public SkillResponse addSkill(Long id, SkillRequest req) {
        JobOffer offer = getEntity(id);
        RequiredSkill skill = skills.save(toSkill(req, offer));
        offer.getRequiredSkills().add(skill);
        return SkillResponse.from(skill);
    }
    public void removeSkill(Long id, Long skillId) {
        JobOffer offer = getEntity(id);
        RequiredSkill skill = offer.getRequiredSkills().stream()
                .filter(s -> s.getId().equals(skillId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Compétence " + skillId + " introuvable sur l'offre " + id));
        offer.getRequiredSkills().remove(skill);            // orphanRemoval = true supprime la ligne
    }
    /** Utilisé aussi par EligibilityService. */
    public JobOffer getEntity(Long id) {
        return offers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Offre " + id + " introuvable"));
    }
    private RequiredSkill toSkill(SkillRequest s, JobOffer offer) {
        return
                RequiredSkill.builder().offer(offer).nom(s.nom()).niveauMinimum(s.niveauMinimum()).build();
    }
}