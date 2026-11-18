package com.hireflow.talents.controller;

import com.hireflow.talents.common.ResourceNotFoundException;
import com.hireflow.talents.dto.CandidateProfileResponse;
import com.hireflow.talents.dto.CandidateResponse;
import com.hireflow.talents.enums.CandidateStatus;
import com.hireflow.talents.service.CandidateService;
import com.hireflow.talents.service.ScreeningTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CandidateController.class)
class CandidateControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean CandidateService candidateService;
    @MockitoBean ScreeningTestService screeningTestService;

    @Test
    void creer_renvoie201() throws Exception {
        when(candidateService.creer(any())).thenReturn(
                new CandidateResponse(1L, "Amine Jamali", "amine@email.com", null, 3, CandidateStatus.NOUVEAU, null));

        mvc.perform(post("/candidates").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Amine Jamali\",\"email\":\"amine@email.com\",\"experienceAnnees\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("NOUVEAU"))
                .andExpect(jsonPath("$.tests").doesNotExist());
    }

    @Test
    void scoreSur120_renvoie400() throws Exception {
        mvc.perform(post("/candidates/1/tests").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"test\":\"Java\",\"score\":120}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void profil_emailAvecPoint_contratFeign() throws Exception {
        when(candidateService.profil("amine@email.com")).thenReturn(new CandidateProfileResponse(
                1L, "Amine Jamali", "amine@email.com", 3, CandidateStatus.EN_PROCESSUS, "Java", 82, 2));

        mvc.perform(get("/candidates/by-email/amine@email.com/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dernierScore").value(82))
                .andExpect(jsonPath("$.statut").value("EN_PROCESSUS"));
    }

    @Test
    void profil_inconnu_renvoie404() throws Exception {
        when(candidateService.profil("inconnu@mail.com")).thenThrow(
                new ResourceNotFoundException("Aucun candidat avec l'email inconnu@mail.com"));

        mvc.perform(get("/candidates/by-email/inconnu@mail.com/profile")).andExpect(status().isNotFound());
    }
}
