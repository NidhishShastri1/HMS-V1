package com.hms.backend.controller;

import com.hms.backend.dto.PatientDto;
import com.hms.backend.dto.PatientMergeRequest;
import com.hms.backend.dto.PatientRegistrationRequest;
import com.hms.backend.dto.PatientUpdateRequest;
import com.hms.backend.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientDto> registerPatient(@Valid @RequestBody PatientRegistrationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        PatientDto savedPatient = patientService.registerPatient(request, currentUsername);
        return ResponseEntity.ok(savedPatient);
    }

    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<PatientDto> getPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(patientService.getPatientById(patientId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientDto>> searchPatients(@RequestParam("query") String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(patientService.searchPatients(query.trim()));
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable String patientId,
            @Valid @RequestBody PatientUpdateRequest request) {
        return ResponseEntity.ok(patientService.updatePatient(patientId, request));
    }

    @PostMapping("/merge")
    public ResponseEntity<?> mergePatients(@Valid @RequestBody PatientMergeRequest request,
            Authentication authentication) {
        if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body("Only administrators can merge patient records.");
        }
        patientService.mergePatients(request.getSourcePatientId(), request.getTargetPatientId());
        return ResponseEntity.ok("Patients merged successfully");
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<?> softDeletePatient(@PathVariable String patientId, Authentication authentication) {
        if (authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(403).body("Only administrators can delete patient records.");
        }
        patientService.softDeletePatient(patientId);
        return ResponseEntity.ok("Patient deleted successfully");
    }
}
