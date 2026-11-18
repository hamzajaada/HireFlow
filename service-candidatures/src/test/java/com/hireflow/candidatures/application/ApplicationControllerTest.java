package com.hireflow.candidatures.application;

import com.hireflow.candidatures.application.controller.ApplicationController;
import com.hireflow.candidatures.application.enums.ApplicationStatus;
import com.hireflow.candidatures.application.service.ApplicationService;
import com.hireflow.candidatures.common.BusinessException;
import com.hireflow.candidatures.common.RejectedApplicationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ApplicationService service;

    @Test
    void depotRefuse_renvoie422() throws Exception {
        when(service.deposer(any())).thenThrow(
                new RejectedApplicationException("Candidature refusée : l'offre 3 n'est pas OUVERTE"));

        mvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"offerId\":3,\"candidatEmail\":\"amine@email.com\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Candidature refusée : l'offre 3 n'est pas OUVERTE"));
    }

    @Test
    void emailInvalide_renvoie400() throws Exception {
        mvc.perform(post("/applications").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"offerId\":1,\"candidatEmail\":\"pas-un-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transitionInterdite_renvoie409() throws Exception {
        when(service.decider(12L, ApplicationStatus.RETENUE)).thenThrow(new BusinessException("Transition interdite"));

        mvc.perform(patch("/applications/12/status").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"RETENUE\"}"))
                .andExpect(status().isConflict());
    }
}
