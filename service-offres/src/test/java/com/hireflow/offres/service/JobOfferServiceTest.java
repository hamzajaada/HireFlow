package com.hireflow.offres.service;
import com.hireflow.offres.dto.JobOfferRequest;
import com.hireflow.offres.dto.JobOfferResponse;
import com.hireflow.offres.dto.SkillRequest;
import com.hireflow.offres.entite.JobOffer;
import com.hireflow.offres.enums.OfferStatus;
import com.hireflow.offres.exception.BusinessException;
import com.hireflow.offres.exception.ResourceNotFoundException;
import com.hireflow.offres.repository.JobOfferRepository;
import com.hireflow.offres.repository.RequiredSkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
/** Tests unitaires : pas de Spring, pas de base. Les repositories sont simulés par Mockito. */
@ExtendWith(MockitoExtension.class)
class JobOfferServiceTest {
    @Mock JobOfferRepository offers;
    @Mock RequiredSkillRepository skills;
    @InjectMocks JobOfferService service;
    @Test
    void creation_offreOuverteAvecSesCompetences() {
// RG1
        when(offers.save(any(JobOffer.class))).thenAnswer(inv -> inv.getArgument(0));
        JobOfferResponse r = service.create(new JobOfferRequest("Développeur Java", "IT", null, null,
                List.of(new SkillRequest("Java", 4))));
        assertEquals(OfferStatus.OUVERTE, r.statut());
        assertEquals(1, r.requiredSkills().size());
        assertEquals(0, r.nombreEntretiens());
    }
    @Test
    void offreInconnue_404() {
        when(offers.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }
    @Test
    void offrePourvue_nonModifiable() {
// RG5
        JobOffer pourvue =
                JobOffer.builder().id(3L).intitule("Comptable").statut(OfferStatus.POURVUE).build();
        when(offers.findById(3L)).thenReturn(Optional.of(pourvue));
        assertThrows(BusinessException.class,
                () -> service.update(3L, new JobOfferRequest("Autre", null, null, null, null)));
    }

    @Test
    void entretienNote_metAJourLaMoyenne() {                              // RG8
        JobOffer offer = JobOffer.builder().id(1L).intitule("Java").build();
        when(offers.findById(1L)).thenReturn(Optional.of(offer));

        service.enregistrerEntretien(1L, 16);
        service.enregistrerEntretien(1L, 12);

        assertEquals(2, offer.getNombreEntretiens());
        assertEquals(14.0, offer.getNoteMoyenne());
    }

    @Test
    void candidatureRetenue_fermeLOffre() {                               // RG9
        JobOffer offer = JobOffer.builder().id(1L).intitule("Java").build();
        when(offers.findById(1L)).thenReturn(Optional.of(offer));

        service.cloturer(1L);

        assertEquals(OfferStatus.POURVUE, offer.getStatut());
    }

    @Test
    void candidatureRetenue_deuxFois_sansErreur() {                       // RG10
        JobOffer offer = JobOffer.builder().id(1L).intitule("Java").statut(OfferStatus.POURVUE).build();
        when(offers.findById(1L)).thenReturn(Optional.of(offer));

        service.cloturer(1L);                                             // même message reçu une 2e fois

        assertEquals(OfferStatus.POURVUE, offer.getStatut());
    }
}
