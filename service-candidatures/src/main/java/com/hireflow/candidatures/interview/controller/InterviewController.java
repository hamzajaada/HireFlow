package com.hireflow.candidatures.interview.controller;

import com.hireflow.candidatures.interview.dto.EvaluationRequest;
import com.hireflow.candidatures.interview.dto.InterviewRequest;
import com.hireflow.candidatures.interview.dto.InterviewResponse;
import com.hireflow.candidatures.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/** Pas de @RequestMapping commun : les URL commencent par /applications ou par /interviews. */
@RestController
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService service;
    @PostMapping("/applications/{applicationId}/interviews")
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse planifier(@PathVariable Long applicationId, @Valid @RequestBody
    InterviewRequest req) {
        return service.planifier(applicationId, req);
    }
    @GetMapping("/applications/{applicationId}/interviews")
    public List<InterviewResponse> lister(@PathVariable Long applicationId) {
        return service.lister(applicationId);
    }
    @GetMapping("/interviews/{id}")
    public InterviewResponse get(@PathVariable Long id) {
        return service.findById(id);
    }
    @PutMapping("/interviews/{id}/evaluation")
    public InterviewResponse noter(@PathVariable Long id, @Valid @RequestBody EvaluationRequest req) {
        return service.noter(id, req);
    }
    @DeleteMapping("/interviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void annuler(@PathVariable Long id) {
        service.annuler(id);
    }
}
