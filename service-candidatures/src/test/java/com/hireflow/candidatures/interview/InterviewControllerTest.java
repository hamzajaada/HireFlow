package com.hireflow.candidatures.interview;

import com.hireflow.candidatures.common.ResourceNotFoundException;
import com.hireflow.candidatures.interview.controller.InterviewController;
import com.hireflow.candidatures.interview.dto.InterviewResponse;
import com.hireflow.candidatures.interview.service.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(InterviewController.class)
class InterviewControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean InterviewService service;
    @Test
    void planifier_renvoie201() throws Exception {
        String demain = LocalDate.now().plusDays(1).toString();
        when(service.planifier(eq(12L), any())).thenReturn(
                new InterviewResponse(5L, 12L, LocalDate.parse(demain), "RH - Mme Idrissi", null, null,
                        null));
        mvc.perform(post("/applications/12/interviews").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"" + demain + "\",\"evaluateur\":\"RH - Mme Idrissi\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicationId").value(12));
    }
    @Test
    void dateDansLePasse_renvoie400() throws Exception { // RG2
        mvc.perform(post("/applications/12/interviews").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"date\":\"2020-01-01\",\"evaluateur\":\"RH - Mme Idrissi\"}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void noteSur25_renvoie400() throws Exception { // RG9
        mvc.perform(put("/interviews/5/evaluation").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":25,\"avis\":\"Excellent\"}"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void entretienInconnu_renvoie404() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Entretien 99 introuvable"));
                mvc.perform(get("/interviews/99")).andExpect(status().isNotFound());
    }
}
