package com.hireflow.talents.service;

import com.hireflow.talents.dto.ApplicationAcceptedEvent;
import com.hireflow.talents.dto.InterviewCompletedEvent;
import com.hireflow.talents.entite.Candidate;
import com.hireflow.talents.entite.ScreeningTest;
import com.hireflow.talents.enums.CandidateStatus;
import com.hireflow.talents.repository.CandidateRepository;
import com.hireflow.talents.repository.ScreeningTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Ce que font les 3 listeners, testé sur les services (avec les vrais CandidateService). */
@ExtendWith(MockitoExtension.class)
class MessagingTraitementsTest {

    @Mock CandidateRepository candidates;
    @Mock ScreeningTestRepository tests;
    CandidateService candidateService;
    Candidate amine;

    @BeforeEach
    void setUp() {
        candidateService = new CandidateService(candidates, tests);
        amine = Candidate.builder().id(1L).nom("Amine Jamali").email("amine@email.com").statut(CandidateStatus.EN_PROCESSUS).build();
        when(candidates.findByEmailIgnoreCase("amine@email.com")).thenReturn(Optional.of(amine));
    }

    @Test
    void entretienNote_devientUnTestSur100_etStatutEvalue() {
        var service = new ScreeningTestService(tests, candidateService);
        when(tests.existsByCandidateIdAndTest(1L, "Entretien · candidature 12")).thenReturn(false);

        assertTrue(service.enregistrerEntretien(new InterviewCompletedEvent(12L, 1L, "amine@email.com", 16, "Très bon")));

        ArgumentCaptor<ScreeningTest> saved = ArgumentCaptor.forClass(ScreeningTest.class);
        verify(tests).save(saved.capture());
        assertEquals(80, saved.getValue().getScore());
        assertEquals(CandidateStatus.EVALUE, amine.getStatut());
    }

    @Test
    void entretienRecuDeuxFois_enregistreUneSeuleFois() {
        var service = new ScreeningTestService(tests, candidateService);
        when(tests.existsByCandidateIdAndTest(1L, "Entretien · candidature 12")).thenReturn(true);

        assertFalse(service.enregistrerEntretien(new InterviewCompletedEvent(12L, 1L, "amine@email.com", 16, "Très bon")));
        verify(tests, never()).save(any());
    }

    @Test
    void embauche_unSeulEmailDeBienvenue() {
        var service = new OnboardingService(candidateService);
        var event = new ApplicationAcceptedEvent(12L, 1L, "amine@email.com", LocalDate.now());

        assertTrue(service.embaucher(event));
        assertEquals(CandidateStatus.EMBAUCHE, amine.getStatut());
        assertFalse(service.embaucher(event));                          // message reçu une deuxième fois
    }
}
