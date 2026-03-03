package com.hms.backend.controller;

import com.hms.backend.dto.*;
import com.hms.backend.model.ClinicalVisitStatus;
import com.hms.backend.service.ClinicalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opd/visits")
public class ClinicalVisitController {

    @Autowired
    private ClinicalService clinicalService;

    @PostMapping("/{id}/clinical-notes")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ClinicalNoteDto> addClinicalNote(@PathVariable("id") String visitId,
            @Valid @RequestBody CreateClinicalNoteRequest request) {
        return ResponseEntity.ok(clinicalService.addClinicalNote(visitId, request));
    }

    @GetMapping("/{id}/clinical-notes")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'SUPERVISOR', 'RECEPTION')")
    public ResponseEntity<List<ClinicalNoteDto>> getNotes(@PathVariable("id") String visitId) {
        return ResponseEntity.ok(clinicalService.getNotesForVisit(visitId));
    }

    @PostMapping("/{id}/diagnosis")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DiagnosisDto> addDiagnosis(@PathVariable("id") String visitId,
            @Valid @RequestBody CreateDiagnosisRequest request) {
        return ResponseEntity.ok(clinicalService.addDiagnosis(visitId, request));
    }

    @GetMapping("/{id}/diagnosis")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'SUPERVISOR', 'RECEPTION')")
    public ResponseEntity<List<DiagnosisDto>> getDiagnoses(@PathVariable("id") String visitId) {
        return ResponseEntity.ok(clinicalService.getDiagnosesForVisit(visitId));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Void> updateVisitStatus(@PathVariable("id") String visitId,
            @Valid @RequestBody UpdateVisitStatusRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isDoctor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        boolean isAdminOrSupervisor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERVISOR"));

        if (request.getStatus() == ClinicalVisitStatus.COMPLETED && !isDoctor) {
            throw new RuntimeException("Only a DOCTOR can mark a visit as COMPLETED");
        }

        if (request.getStatus() == ClinicalVisitStatus.CLOSED && !isAdminOrSupervisor) {
            throw new RuntimeException("Only an ADMIN or SUPERVISOR can mark a visit as CLOSED");
        }

        clinicalService.updateVisitStatus(visitId, request);
        return ResponseEntity.ok().build();
    }
}
