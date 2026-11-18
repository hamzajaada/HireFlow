package com.hireflow.talents.service;

import com.hireflow.talents.dto.InterviewCompletedEvent;
import com.hireflow.talents.dto.ScreeningTestRequest;
import com.hireflow.talents.dto.ScreeningTestResponse;
import com.hireflow.talents.entite.Candidate;
import com.hireflow.talents.entite.ScreeningTest;
import com.hireflow.talents.enums.CandidateStatus;
import com.hireflow.talents.repository.ScreeningTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScreeningTestService {

    private final ScreeningTestRepository tests;
    private final CandidateService candidateService;

    /** RG3, RG4 : score de 0 à 100, date du jour par défaut. */
    public ScreeningTestResponse ajouter(Long candidateId, ScreeningTestRequest req) {
        Candidate c = candidateService.getEntity(candidateId);         // 404 si inconnu
        ScreeningTest t = tests.save(ScreeningTest.builder()
                .candidate(c)
                .test(req.test())
                .score(req.score())
                .date(req.date() != null ? req.date() : LocalDate.now())
                .build());
        return ScreeningTestResponse.from(t);
    }

    @Transactional(readOnly = true)
    public List<ScreeningTestResponse> lister(Long candidateId) {
        candidateService.getEntity(candidateId);                        // 404 si inconnu
        return tests.findByCandidateIdOrderByDateDescIdDesc(candidateId).stream()
                .map(ScreeningTestResponse::from).toList();
    }

    /** RG6 : l'entretien noté devient un test « Entretien », une seule fois par candidature. */
    public boolean enregistrerEntretien(InterviewCompletedEvent e) {
        Candidate c = candidateService.creerOuMettreAJour(e.candidatEmail());
        String nom = "Entretien · candidature " + e.applicationId();
        if (tests.existsByCandidateIdAndTest(c.getId(), nom)) {
            log.info("Entretien de la candidature {} déjà enregistré : message ignoré", e.applicationId());
            return false;
        }
        tests.save(ScreeningTest.builder()
                .candidate(c)
                .test(nom)
                .score(e.note() * 5)                                    // note sur 20 -> score sur 100
                .date(LocalDate.now())
                .build());
        candidateService.avancer(c, CandidateStatus.EVALUE);
        return true;
    }
}
