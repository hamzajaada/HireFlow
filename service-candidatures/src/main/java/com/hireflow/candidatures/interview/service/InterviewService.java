package com.hireflow.candidatures.interview.service;

import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.application.enums.ApplicationStatus;
import com.hireflow.candidatures.application.service.ApplicationService;
import com.hireflow.candidatures.common.BusinessException;
import com.hireflow.candidatures.common.ResourceNotFoundException;
import com.hireflow.candidatures.interview.client.CandidateProfile;
import com.hireflow.candidatures.interview.client.TalentClient;
import com.hireflow.candidatures.interview.dto.CandidateSummary;
import com.hireflow.candidatures.interview.dto.EvaluationRequest;
import com.hireflow.candidatures.interview.dto.InterviewRequest;
import com.hireflow.candidatures.interview.dto.InterviewResponse;
import com.hireflow.candidatures.interview.entite.Interview;
import com.hireflow.candidatures.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class InterviewService {
    private final InterviewRepository interviews;
    private final ApplicationService applicationService; // code écrit avec Yasmine au kick-off
    private final TalentClient talentClient; // le vrai Feign, ou TalentClientFake avec "fake"
    /** Planifier : RG1 à RG5. */
    public InterviewResponse planifier(Long applicationId, InterviewRequest req) {
        Application app = applicationService.passerEnEntretien(applicationId); // 404 si inconnue, 409 si décidée
        CandidateProfile profil = talentClient.profil(app.getCandidatEmail()); // null si indisponible
        Interview interview = interviews.save(Interview.builder()
                .application(app)
                .date(req.date())
                .evaluateur(req.evaluateur())
                .build());
        return InterviewResponse.from(interview, CandidateSummary.from(profil));
    }
    @Transactional(readOnly = true)
    public List<InterviewResponse> lister(Long applicationId) {
        applicationService.getEntity(applicationId); // 404 si inconnue
        return
                interviews.findByApplicationId(applicationId).stream().map(InterviewResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public InterviewResponse findById(Long id) {
        return InterviewResponse.from(getEntity(id));
    }
    private Interview getEntity(Long id) {
        return interviews.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entretien " + id + " introuvable"));
    }

    /** Noter : RG6 à RG9. */
    public InterviewResponse noter(Long id, EvaluationRequest req) {
        Interview interview = getEntity(id);
        if (interview.getNote() != null) {
            throw new BusinessException("L'entretien " + id + " est déjà noté");
        }
        if (interview.getApplication().getStatut() != ApplicationStatus.EN_ENTRETIEN) {
            throw new BusinessException("La candidature est déjà " +
                    interview.getApplication().getStatut()
                    + " : on ne note plus ses entretiens");
        }
        interview.setNote(req.note());
        interview.setAvis(req.avis());
        return InterviewResponse.from(interview);
    }
    /** Annuler : RG10. */
    public void annuler(Long id) {
        Interview interview = getEntity(id);
        if (interview.getNote() != null) {
            throw new BusinessException("L'entretien " + id + " est déjà noté, il ne peut plus être annulé");
        }
        interviews.delete(interview);
    }

}

