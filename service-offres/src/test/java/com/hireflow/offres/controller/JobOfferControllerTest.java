package com.hireflow.offres.controller;
import com.hireflow.offres.dto.JobOfferResponse;
import com.hireflow.offres.enums.OfferStatus;
import com.hireflow.offres.exception.ResourceNotFoundException;
import java.util.List;
import com.hireflow.offres.service.EligibilityService;
import com.hireflow.offres.service.JobOfferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/** Teste seulement la couche web (HTTP, validation, format d'erreur). Le service est simulé. */
@WebMvcTest(JobOfferController.class)
class JobOfferControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean JobOfferService service;
    @MockitoBean EligibilityService eligibility;
    @Test
    void offreInconnue_renvoie404AuFormatCommun() throws Exception {
        when(service.findById(99L)).thenThrow(new ResourceNotFoundException("Offre 99 introuvable"));
        mvc.perform(get("/offers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Offre 99 introuvable"))
                .andExpect(jsonPath("$.path").value("/offers/99"));
    }
    @Test
    void intituleVide_renvoie400() throws Exception {
        mvc.perform(post("/offers").contentType(MediaType.APPLICATION_JSON).content(" {\"intitule\":\"\"}")).andExpect(status().isBadRequest());
        }
        @Test
        void statutInconnu_renvoie400() throws Exception {
            mvc.perform(get("/offers?statut=FERMEE"))
                    .andExpect(status().isBadRequest());
        }
    
    @Test
    void progress_renvoieLAvancement() throws Exception {                 // RG8, GET /offers/{id}/progress
        when(service.findById(1L)).thenReturn(new JobOfferResponse(1L, "Développeur Java", "IT", null, null,
                OfferStatus.OUVERTE, 2, 14.0, List.of()));
        mvc.perform(get("/offers/1/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerId").value(1))
                .andExpect(jsonPath("$.statut").value("OUVERTE"))
                .andExpect(jsonPath("$.nombreEntretiens").value(2))
                .andExpect(jsonPath("$.noteMoyenne").value(14.0));
    }
}
