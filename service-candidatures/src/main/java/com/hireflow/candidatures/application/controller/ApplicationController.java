package com.hireflow.candidatures.application.controller;

import com.hireflow.candidatures.application.dto.ApplicationRequest;
import com.hireflow.candidatures.application.dto.ApplicationResponse;
import com.hireflow.candidatures.application.dto.StatusChangeRequest;
import com.hireflow.candidatures.application.enums.ApplicationStatus;
import com.hireflow.candidatures.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse deposer(@Valid @RequestBody ApplicationRequest req) {
        return service.deposer(req);
    }

    @GetMapping
    public List<ApplicationResponse> list(@RequestParam(required = false) Long offerId,
                                          @RequestParam(required = false) ApplicationStatus statut) {
        return service.findAll(offerId, statut);
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse decider(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest req) {
        return service.decider(id, req.statut());
    }
}
