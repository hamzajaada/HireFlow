package com.hireflow.candidatures.application;

import com.hireflow.candidatures.application.client.EligibilityResponse;
import com.hireflow.candidatures.application.client.EligibilityResponse.MissingSkill;
import com.hireflow.candidatures.application.client.OffreClient;
import com.hireflow.candidatures.application.dto.ApplicationRequest;
import com.hireflow.candidatures.application.dto.DeclaredSkill;
import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.application.enums.ApplicationStatus;
import com.hireflow.candidatures.application.repository.ApplicationRepository;
import com.hireflow.candidatures.application.service.ApplicationService;
import com.hireflow.candidatures.common.BusinessException;
import com.hireflow.candidatures.common.RejectedApplicationException;
import com.hireflow.candidatures.common.StatusChange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** OffreClient est simulé : ces tests ne dépendent ni de Hamza ni des fichiers fake. */
@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock ApplicationRepository applications;
    @Mock OffreClient offreClient;
    @Mock ApplicationEventPublisher springEvents;
    @InjectMocks ApplicationService service;

    private final ApplicationRequest req = new ApplicationRequest(1L, "Amine@Email.com",
            "https://cv.example.com/amine.pdf", "Motivé", List.of(new DeclaredSkill("Java", 4)));

    @Test
    void offreFermee_refusAvec422_etRienEnregistre() {                     // RG3
        when(offreClient.verifier(eq(1L), any()))
                .thenReturn(new EligibilityResponse(1L, "POURVUE", false, true, List.of(), false));

        assertThrows(RejectedApplicationException.class, () -> service.deposer(req));
        verify(applications, never()).save(any());
    }

    @Test
    void competenceManquante_refusAvec422() {                              // RG4
        when(offreClient.verifier(eq(1L), any())).thenReturn(new EligibilityResponse(1L, "OUVERTE", true, false,
                List.of(new MissingSkill("Spring Boot", 3, 1)), false));

        var e = assertThrows(RejectedApplicationException.class, () -> service.deposer(req));
        assertEquals("Candidature refusée : compétences insuffisantes : Spring Boot (minimum 3)", e.getMessage());
    }

    @Test
    void depotValide_enregistreDeposee_etPublieSubmitted() {               // RG1, RG5
        when(offreClient.verifier(eq(1L), any()))
                .thenReturn(new EligibilityResponse(1L, "OUVERTE", true, true, List.of(), true));
        when(applications.save(any())).thenAnswer(inv -> {
            Application a = inv.getArgument(0);
            a.setId(12L);
            return a;
        });

        var res = service.deposer(req);

        assertEquals(ApplicationStatus.DEPOSEE, res.statut());
        assertEquals("amine@email.com", res.candidatEmail());
    }

    @Test
    void doublon_refusAvec409_sansAppelerFeign() {                           // RG2
        when(applications.existsByOfferIdAndCandidatEmailIgnoreCase(1L, "Amine@Email.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.deposer(req));
        verify(offreClient, never()).verifier(any(), any());
    }

    @Test
    void retenirUneCandidatureDeposee_interdit() {                         // RG7
        Application app = Application.builder().id(12L).offerId(1L).candidatEmail("a@b.com").build();
        when(applications.findById(12L)).thenReturn(Optional.of(app));

        assertThrows(BusinessException.class, () -> service.decider(12L, ApplicationStatus.RETENUE));
    }

    @Test
    void retenirUneCandidatureEnEntretien_publieAccepted_etStatusChange() { // RG8 + temps réel
        Application app = Application.builder().id(12L).offerId(1L).candidatEmail("a@b.com")
                .statut(ApplicationStatus.EN_ENTRETIEN).build();
        when(applications.findById(12L)).thenReturn(Optional.of(app));

        var res = service.decider(12L, ApplicationStatus.RETENUE);

        assertEquals(ApplicationStatus.RETENUE, res.statut());
        verify(springEvents).publishEvent(any(StatusChange.class));
    }

    @Test
    void deuxiemeRetenuSurLaMemeOffre_interdit() {                         // RG9
        Application app = Application.builder().id(13L).offerId(1L).candidatEmail("b@c.com")
                .statut(ApplicationStatus.EN_ENTRETIEN).build();
        when(applications.findById(13L)).thenReturn(Optional.of(app));
        when(applications.existsByOfferIdAndStatut(1L, ApplicationStatus.RETENUE)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.decider(13L, ApplicationStatus.RETENUE));
    }

    @Test
    void passerEnEntretien_depuisDeposee_publieStatusChange() {            // RG10
        Application app = Application.builder().id(2L).offerId(2L).candidatEmail("sara@email.com").build();
        when(applications.findById(2L)).thenReturn(Optional.of(app));

        service.passerEnEntretien(2L);

        assertEquals(ApplicationStatus.EN_ENTRETIEN, app.getStatut());
        verify(springEvents).publishEvent(any(StatusChange.class));
    }
}
