package com.hireflow.talents.service;

import com.hireflow.talents.common.BusinessException;
import com.hireflow.talents.common.ResourceNotFoundException;
import com.hireflow.talents.dto.CandidateProfileResponse;
import com.hireflow.talents.dto.CandidateRequest;
import com.hireflow.talents.dto.CandidateResponse;
import com.hireflow.talents.dto.CandidateUpdateRequest;
import com.hireflow.talents.dto.ScreeningTestResponse;
import com.hireflow.talents.entite.Candidate;
import com.hireflow.talents.entite.ScreeningTest;
import com.hireflow.talents.enums.CandidateStatus;
import com.hireflow.talents.repository.CandidateRepository;
import com.hireflow.talents.repository.ScreeningTestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidateService {

    private final CandidateRepository candidates;
    private final ScreeningTestRepository tests;

    /** RG1, RG2 : email obligatoire et unique (sans tenir compte des majuscules). */
    public CandidateResponse creer(CandidateRequest req) {
        if (candidates.existsByEmailIgnoreCase(req.email())) {
            throw new BusinessException("Un candidat existe déjà avec l'email " + req.email());
        }
        Candidate c = candidates.save(Candidate.builder()
                .nom(req.nom())
                .email(req.email().toLowerCase())
                .telephone(req.telephone())
                .experienceAnnees(req.experienceAnnees())
                .build());                                              // statut NOUVEAU par défaut
        return CandidateResponse.court(c);
    }

    @Transactional(readOnly = true)
    public List<CandidateResponse> lister(CandidateStatus statut) {
        List<Candidate> liste = statut == null ? candidates.findAll() : candidates.findByStatut(statut);
        return liste.stream().map(CandidateResponse::court).toList();
    }

    @Transactional(readOnly = true)
    public CandidateResponse findById(Long id) {
        return detail(getEntity(id));
    }

    @Transactional(readOnly = true)
    public CandidateResponse findByEmail(String email) {
        return detail(getByEmail(email));
    }

    public CandidateResponse modifier(Long id, CandidateUpdateRequest req) {
        Candidate c = getEntity(id);
        c.setNom(req.nom());
        c.setTelephone(req.telephone());
        c.setExperienceAnnees(req.experienceAnnees());
        return CandidateResponse.court(c);
    }

    public void supprimer(Long id) {
        candidates.delete(getEntity(id));                               // ses tests partent avec lui (cascade)
    }

    /** Feign ② pour Abderahman : le parcours résumé et le dernier test. */
    @Transactional(readOnly = true)
    public CandidateProfileResponse profil(String email) {
        Candidate c = getByEmail(email);
        List<ScreeningTest> historique = tests.findByCandidateIdOrderByDateDescIdDesc(c.getId());
        ScreeningTest dernier = historique.isEmpty() ? null : historique.get(0);
        return new CandidateProfileResponse(c.getId(), c.getNom(), c.getEmail(), c.getExperienceAnnees(),
                c.getStatut(),
                dernier == null ? null : dernier.getTest(),
                dernier == null ? null : dernier.getScore(),
                historique.size());
    }

    /** RG5 : appelé par RabbitMQ. Trouve le candidat par email, ou le crée avec un nom provisoire. */
    public Candidate creerOuMettreAJour(String email) {
        Candidate c = candidates.findByEmailIgnoreCase(email).orElseGet(() -> {
            log.info("Nouveau candidat créé depuis RabbitMQ : {}", email);
            return candidates.save(Candidate.builder()
                    .nom(email.substring(0, email.indexOf('@')))       // nom provisoire : la partie avant @
                    .email(email.toLowerCase())
                    .build());
        });
        avancer(c, CandidateStatus.EN_PROCESSUS);
        return c;
    }

    /** RG8 : le statut ne recule jamais. Renvoie true s'il a changé. */
    public boolean avancer(Candidate c, CandidateStatus nouveau) {
        if (nouveau.ordinal() <= c.getStatut().ordinal()) {
            return false;
        }
        c.setStatut(nouveau);
        return true;
    }

    public Candidate getEntity(Long id) {
        return candidates.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidat " + id + " introuvable"));
    }

    public Candidate getByEmail(String email) {
        return candidates.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun candidat avec l'email " + email));
    }

    private CandidateResponse detail(Candidate c) {
        return CandidateResponse.detail(c,
                tests.findByCandidateIdOrderByDateDescIdDesc(c.getId()).stream()
                        .map(ScreeningTestResponse::from).toList());
    }
}
