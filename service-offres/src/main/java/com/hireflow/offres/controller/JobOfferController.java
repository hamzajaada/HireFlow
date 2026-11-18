package com.hireflow.offres.controller;
import com.hireflow.offres.dto.EligibilityRequest;
import com.hireflow.offres.dto.EligibilityResponse;
import com.hireflow.offres.dto.JobOfferRequest;
import com.hireflow.offres.dto.JobOfferResponse;
import com.hireflow.offres.dto.SkillRequest;
import com.hireflow.offres.dto.SkillResponse;
import com.hireflow.offres.enums.OfferStatus;
import com.hireflow.offres.service.EligibilityService;
import com.hireflow.offres.service.JobOfferService;
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
@RequestMapping("/offers")
@RequiredArgsConstructor
public class JobOfferController {
    private final JobOfferService service;
    private final EligibilityService eligibility;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobOfferResponse create(@Valid @RequestBody JobOfferRequest req) {
        return service.create(req);
    }
    @GetMapping
    public List<JobOfferResponse> list(@RequestParam(required = false) OfferStatus statut) {
        return service.findAll(statut);
    }
    @GetMapping("/{id}")
    public JobOfferResponse get(@PathVariable Long id) {
        return service.findById(id);
    }
    @PutMapping("/{id}")
    public JobOfferResponse update(@PathVariable Long id, @Valid @RequestBody JobOfferRequest req) {
        return service.update(id, req);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @PostMapping("/{id}/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse addSkill(@PathVariable Long id, @Valid @RequestBody SkillRequest req) {
        return service.addSkill(id, req);
    }
    @DeleteMapping("/{id}/skills/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSkill(@PathVariable Long id, @PathVariable Long skillId) {
        service.removeSkill(id, skillId);
    }
    /** Appelé par le Feign
     ①
     de Yasmine. */
    @PostMapping("/{id}/eligibility")
    public EligibilityResponse eligibility(@PathVariable Long id, @RequestBody EligibilityRequest req) {
        return eligibility.verifier(id, req);
    }
}