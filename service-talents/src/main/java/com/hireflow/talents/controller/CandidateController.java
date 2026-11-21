package com.hireflow.talents.controller;

import com.hireflow.talents.common.ApiError;
import com.hireflow.talents.dto.CandidateProfileResponse;
import com.hireflow.talents.dto.CandidateRequest;
import com.hireflow.talents.dto.CandidateResponse;
import com.hireflow.talents.dto.CandidateUpdateRequest;
import com.hireflow.talents.dto.ScreeningTestRequest;
import com.hireflow.talents.dto.ScreeningTestResponse;
import com.hireflow.talents.enums.CandidateStatus;
import com.hireflow.talents.service.CandidateService;
import com.hireflow.talents.service.ScreeningTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Candidats", description = "CRUD des candidats et de leurs tests de compétences (RG1 à RG4), profil pour Feign ②")
@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;
    private final ScreeningTestService screeningTestService;

    @Operation(summary = "Créer un candidat", description = "Nom et email obligatoires, email unique (RG1, RG2). Statut initial : NOUVEAU.")
    @ApiResponse(responseCode = "201", description = "Candidat créé")
    @ApiResponse(responseCode = "400", description = "Données invalides (nom vide, email mal formé, expérience hors 0-50)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Un candidat existe déjà avec cet email (RG2)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidateResponse creer(@Valid @RequestBody CandidateRequest req) {
        return candidateService.creer(req);
    }

    @Operation(summary = "Lister les candidats", description = "Filtre optionnel par statut : NOUVEAU, EN_PROCESSUS, EVALUE, EMBAUCHE.")
    @GetMapping
    public List<CandidateResponse> lister(@RequestParam(required = false) CandidateStatus statut) {
        return candidateService.lister(statut);
    }

    @Operation(summary = "Fiche d'un candidat", description = "Le candidat et l'historique de ses tests, du plus récent au plus ancien.")
    @ApiResponse(responseCode = "200", description = "Candidat trouvé")
    @ApiResponse(responseCode = "404", description = "Candidat introuvable", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}")
    public CandidateResponse get(@PathVariable Long id) {
        return candidateService.findById(id);
    }

    @Operation(summary = "Fiche d'un candidat par email", description = "Même réponse que GET /candidates/{id}. Majuscules ignorées.")
    @ApiResponse(responseCode = "200", description = "Candidat trouvé")
    @ApiResponse(responseCode = "404", description = "Aucun candidat avec cet email", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/by-email/{email}")
    public CandidateResponse getByEmail(@PathVariable String email) {
        return candidateService.findByEmail(email);
    }

    /** Feign ② : appelé par Abderahman avant un entretien. */
    @Operation(summary = "Profil résumé (Feign ②)",
            description = "Appelé par service-candidatures avant de planifier un entretien : expérience, statut, "
                    + "dernier test et son score, nombre de tests. Contrat partagé : ne pas renommer les champs.")
    @ApiResponse(responseCode = "200", description = "Profil trouvé")
    @ApiResponse(responseCode = "404", description = "Aucun candidat avec cet email (candidatures affiche alors un résumé vide)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/by-email/{email}/profile")
    public CandidateProfileResponse profil(@PathVariable String email) {
        return candidateService.profil(email);
    }

    @Operation(summary = "Modifier un candidat", description = "Nom, téléphone et expérience. L'email et le statut ne se modifient pas ici.")
    @ApiResponse(responseCode = "200", description = "Candidat modifié")
    @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Candidat introuvable", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{id}")
    public CandidateResponse modifier(@PathVariable Long id, @Valid @RequestBody CandidateUpdateRequest req) {
        return candidateService.modifier(id, req);
    }

    @Operation(summary = "Supprimer un candidat", description = "Ses tests sont supprimés avec lui.")
    @ApiResponse(responseCode = "204", description = "Candidat supprimé")
    @ApiResponse(responseCode = "404", description = "Candidat introuvable", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        candidateService.supprimer(id);
    }

    @Operation(summary = "Ajouter un test de compétences", description = "Score de 0 à 100 (RG3, RG4). Sans date, la date du jour est utilisée.")
    @ApiResponse(responseCode = "201", description = "Test ajouté")
    @ApiResponse(responseCode = "400", description = "Score hors de 0-100, nom du test vide ou date dans le futur", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Candidat introuvable", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/{id}/tests")
    @ResponseStatus(HttpStatus.CREATED)
    public ScreeningTestResponse ajouterTest(@PathVariable Long id, @Valid @RequestBody ScreeningTestRequest req) {
        return screeningTestService.ajouter(id, req);
    }

    @Operation(summary = "Tests d'un candidat", description = "Du plus récent au plus ancien, y compris les entretiens notés reçus par RabbitMQ.")
    @ApiResponse(responseCode = "200", description = "Liste des tests")
    @ApiResponse(responseCode = "404", description = "Candidat introuvable", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/{id}/tests")
    public List<ScreeningTestResponse> tests(@PathVariable Long id) {
        return screeningTestService.lister(id);
    }
}
