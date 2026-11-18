package com.hireflow.talents.service;

import com.hireflow.talents.common.BusinessException;
import com.hireflow.talents.common.ResourceNotFoundException;
import com.hireflow.talents.dto.CandidateRequest;
import com.hireflow.talents.entite.Candidate;
import com.hireflow.talents.entite.ScreeningTest;
import com.hireflow.talents.enums.CandidateStatus;
import com.hireflow.talents.repository.CandidateRepository;
import com.hireflow.talents.repository.ScreeningTestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateServiceTest {

    @Mock CandidateRepository candidates;
    @Mock ScreeningTestRepository tests;
    @InjectMocks CandidateService service;

    private Candidate amine(CandidateStatus statut) {
        return Candidate.builder().id(1L).nom("Amine Jamali").email("amine@email.com").experienceAnnees(3).statut(statut).build();
    }

    @Test
    void emailDejaUtilise_interdit() {
        when(candidates.existsByEmailIgnoreCase("Amine@Email.com")).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> service.creer(new CandidateRequest("Amine Jamali", "Amine@Email.com", null, 3)));
        verify(candidates, never()).save(any());
    }

    @Test
    void rabbit_candidatInconnu_creeAvecNomProvisoire() {
        when(candidates.findByEmailIgnoreCase("youssef@email.com")).thenReturn(Optional.empty());
        when(candidates.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Candidate c = service.creerOuMettreAJour("youssef@email.com");

        assertEquals("youssef", c.getNom());
        assertEquals(CandidateStatus.EN_PROCESSUS, c.getStatut());
    }

    @Test
    void rabbit_candidatConnu_pasDeDoublon() {
        when(candidates.findByEmailIgnoreCase("amine@email.com")).thenReturn(Optional.of(amine(CandidateStatus.NOUVEAU)));

        Candidate c = service.creerOuMettreAJour("amine@email.com");

        assertEquals(CandidateStatus.EN_PROCESSUS, c.getStatut());
        verify(candidates, never()).save(any());
    }

    @Test
    void statut_neReculeJamais() {
        Candidate c = amine(CandidateStatus.EMBAUCHE);

        assertFalse(service.avancer(c, CandidateStatus.EN_PROCESSUS));
        assertEquals(CandidateStatus.EMBAUCHE, c.getStatut());
    }

    @Test
    void profil_donneLeDernierTest() {
        Candidate c = amine(CandidateStatus.EN_PROCESSUS);
        when(candidates.findByEmailIgnoreCase("amine@email.com")).thenReturn(Optional.of(c));
        when(tests.findByCandidateIdOrderByDateDescIdDesc(1L)).thenReturn(List.of(
                ScreeningTest.builder().candidate(c).test("Java").score(82).date(LocalDate.of(2026, 11, 5)).build(),
                ScreeningTest.builder().candidate(c).test("SQL").score(70).date(LocalDate.of(2026, 10, 28)).build()));

        var p = service.profil("amine@email.com");

        assertEquals("Java", p.dernierTest());
        assertEquals(82, p.dernierScore());
        assertEquals(2, p.nombreTests());
    }

    @Test
    void profil_sansTest_champsVides() {
        when(candidates.findByEmailIgnoreCase("amine@email.com")).thenReturn(Optional.of(amine(CandidateStatus.NOUVEAU)));
        when(tests.findByCandidateIdOrderByDateDescIdDesc(1L)).thenReturn(List.of());

        var p = service.profil("amine@email.com");

        assertNull(p.dernierScore());
        assertEquals(0, p.nombreTests());
    }

    @Test
    void profil_candidatInconnu_404() {
        when(candidates.findByEmailIgnoreCase("inconnu@mail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.profil("inconnu@mail.com"));
    }
}
