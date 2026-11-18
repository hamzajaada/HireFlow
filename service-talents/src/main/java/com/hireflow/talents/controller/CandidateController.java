package com.hireflow.talents.controller;

import com.hireflow.talents.dto.CandidateProfileResponse;
import com.hireflow.talents.dto.CandidateRequest;
import com.hireflow.talents.dto.CandidateResponse;
import com.hireflow.talents.dto.CandidateUpdateRequest;
import com.hireflow.talents.dto.ScreeningTestRequest;
import com.hireflow.talents.dto.ScreeningTestResponse;
import com.hireflow.talents.enums.CandidateStatus;
import com.hireflow.talents.service.CandidateService;
import com.hireflow.talents.service.ScreeningTestService;
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

@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;
    private final ScreeningTestService screeningTestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CandidateResponse creer(@Valid @RequestBody CandidateRequest req) {
        return candidateService.creer(req);
    }

    @GetMapping
    public List<CandidateResponse> lister(@RequestParam(required = false) CandidateStatus statut) {
        return candidateService.lister(statut);
    }

    @GetMapping("/{id}")
    public CandidateResponse get(@PathVariable Long id) {
        return candidateService.findById(id);
    }

    @GetMapping("/by-email/{email}")
    public CandidateResponse getByEmail(@PathVariable String email) {
        return candidateService.findByEmail(email);
    }

    /** Feign ② : appelé par Abderahman avant un entretien. */
    @GetMapping("/by-email/{email}/profile")
    public CandidateProfileResponse profil(@PathVariable String email) {
        return candidateService.profil(email);
    }

    @PutMapping("/{id}")
    public CandidateResponse modifier(@PathVariable Long id, @Valid @RequestBody CandidateUpdateRequest req) {
        return candidateService.modifier(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        candidateService.supprimer(id);
    }

    @PostMapping("/{id}/tests")
    @ResponseStatus(HttpStatus.CREATED)
    public ScreeningTestResponse ajouterTest(@PathVariable Long id, @Valid @RequestBody ScreeningTestRequest req) {
        return screeningTestService.ajouter(id, req);
    }

    @GetMapping("/{id}/tests")
    public List<ScreeningTestResponse> tests(@PathVariable Long id) {
        return screeningTestService.lister(id);
    }
}
