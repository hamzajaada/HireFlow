package com.hireflow.offres.service;
import com.hireflow.offres.dto.CandidateSkill;
import com.hireflow.offres.dto.EligibilityRequest;
import com.hireflow.offres.dto.EligibilityResponse;
import com.hireflow.offres.entite.JobOffer;
import com.hireflow.offres.entite.RequiredSkill;
import com.hireflow.offres.enums.OfferStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class EligibilityServiceTest {
    @Mock JobOfferService jobOfferService;
    @InjectMocks EligibilityService service;
    private JobOffer offreJava() {
        JobOffer offer = JobOffer.builder().id(1L).intitule("Développeur Java").build();

        offer.getRequiredSkills().add(RequiredSkill.builder().offer(offer).nom("Java").niveauMinimum(4).build())
        ;
        offer.getRequiredSkills().add(RequiredSkill.builder().offer(offer).nom("Spring Boot").niveauMinimum(3).build());
        return offer;
    }
    @Test
    void toutCouvert_eligible() {                                         // RG6
        when(jobOfferService.getEntity(1L)).thenReturn(offreJava());
        EligibilityResponse r = service.verifier(1L, new EligibilityRequest("amine@email.com",
                List.of(new CandidateSkill("java", 5), new CandidateSkill(" Spring Boot ", 3))));
        assertTrue(r.eligible());
        assertTrue(r.competencesManquantes().isEmpty());
    }
    @Test
    void niveauInsuffisant_nonEligible() {                                // RG7
        when(jobOfferService.getEntity(1L)).thenReturn(offreJava());
        EligibilityResponse r = service.verifier(1L, new EligibilityRequest("amine@email.com",
                List.of(new CandidateSkill("Java", 4), new CandidateSkill("Spring Boot", 1))));
        assertFalse(r.eligible());
        assertTrue(r.ouverte());
        assertEquals("Spring Boot", r.competencesManquantes().get(0).nom());
        assertEquals(1, r.competencesManquantes().get(0).niveauCandidat());
    }
    @Test
    void offrePourvue_nonEligible() {                                     // RG6
        JobOffer offer =
                JobOffer.builder().id(3L).intitule("Comptable").statut(OfferStatus.POURVUE).build();
        when(jobOfferService.getEntity(3L)).thenReturn(offer);
        EligibilityResponse r = service.verifier(3L, new EligibilityRequest("a@b.com", List.of()));
        assertFalse(r.ouverte());
        assertFalse(r.eligible());
    }
}