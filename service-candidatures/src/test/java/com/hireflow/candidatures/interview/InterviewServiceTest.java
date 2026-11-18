package com.hireflow.candidatures.interview;

import com.hireflow.candidatures.application.entite.Application;
import com.hireflow.candidatures.application.enums.ApplicationStatus;
import com.hireflow.candidatures.application.service.ApplicationService;
import com.hireflow.candidatures.common.BusinessException;
import com.hireflow.candidatures.interview.client.CandidateProfile;
import com.hireflow.candidatures.interview.client.TalentClient;
import com.hireflow.candidatures.interview.dto.EvaluationRequest;
import com.hireflow.candidatures.interview.dto.InterviewRequest;
import com.hireflow.candidatures.interview.entite.Interview;
import com.hireflow.candidatures.interview.repository.InterviewRepository;
import com.hireflow.candidatures.interview.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
/** ApplicationService et TalentClient sont simulés : ces tests ne dépendent ni de Yasmine ni de Douae.
 */
@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {
    @Mock InterviewRepository interviews;
    @Mock ApplicationService applicationService;
    @Mock TalentClient talentClient;
    @InjectMocks InterviewService service;
    private Application app(ApplicationStatus statut) {
        return
                Application.builder().id(12L).offerId(1L).candidatEmail("amine@email.com").statut(statut).build();
    }
    @Test
    void planifier_renvoieLeResumeDuCandidat() { // RG1 à RG3
        when(applicationService.passerEnEntretien(12L)).thenReturn(app(ApplicationStatus.EN_ENTRETIEN));
        when(talentClient.profil("amine@email.com"))
                .thenReturn(new CandidateProfile(1L, "Amine", "amine@email.com", 3, "EN_PROCESSUS",
                        "Java", 78, 1));
        when(interviews.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var res = service.planifier(12L, new InterviewRequest(LocalDate.now().plusDays(3), "RH - Mme Idrissi"));
                assertEquals(78, res.candidat().dernierScore());
        assertEquals(12L, res.applicationId());
    }
    @Test
    void talentsIndisponible_entretienPlanifieSansResume() { // RG4
        when(applicationService.passerEnEntretien(12L)).thenReturn(app(ApplicationStatus.EN_ENTRETIEN));
        when(talentClient.profil(any())).thenReturn(null); // ce que renvoie le fallback
        when(interviews.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var res = service.planifier(12L, new InterviewRequest(LocalDate.now(), "RH - Mme Idrissi"));
        assertNull(res.candidat());
    }
    @Test
    void noter_publieInterviewCompleted() { // RG6
        Interview i = Interview.builder().id(5L).application(app(ApplicationStatus.EN_ENTRETIEN))
                .evaluateur("RH - Mme Idrissi").build();
        when(interviews.findById(5L)).thenReturn(Optional.of(i));
        var res = service.noter(5L, new EvaluationRequest(16, "Très bon profil"));
        assertEquals(16, res.note());
    }
    @Test
    void noterDeuxFois_interdit() { // RG7
        Interview i =
                Interview.builder().id(5L).application(app(ApplicationStatus.EN_ENTRETIEN)).note(14).build();
        when(interviews.findById(5L)).thenReturn(Optional.of(i));
        assertThrows(BusinessException.class, () -> service.noter(5L, new EvaluationRequest(16, "")));
    }
    @Test
    void noterApresDecision_interdit() { // RG8
        Interview i = Interview.builder().id(5L).application(app(ApplicationStatus.RETENUE)).build();
        when(interviews.findById(5L)).thenReturn(Optional.of(i));
        assertThrows(BusinessException.class, () -> service.noter(5L, new EvaluationRequest(16, "")));
    }
    @Test
    void annulerUnEntretienNote_interdit() { // RG10
        Interview i =
                Interview.builder().id(5L).application(app(ApplicationStatus.EN_ENTRETIEN)).note(12).build();
        when(interviews.findById(5L)).thenReturn(Optional.of(i));
        assertThrows(BusinessException.class, () -> service.annuler(5L));
        verify(interviews, never()).delete(any());
    }
}
