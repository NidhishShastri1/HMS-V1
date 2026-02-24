package com.hms.backend.service;

import com.hms.backend.dto.PatientDto;
import com.hms.backend.dto.PatientRegistrationRequest;
import com.hms.backend.dto.PatientUpdateRequest;
import com.hms.backend.exception.DuplicatePatientException;
import com.hms.backend.model.Patient;
import com.hms.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientDto registerPatient(PatientRegistrationRequest request, String createdBy) {
        if (request.getDateOfBirth() == null && request.getAge() == null) {
            throw new IllegalArgumentException("Either Date of Birth or Age must be provided.");
        }

        LocalDate dob = null;
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isBlank()) {
            dob = LocalDate.parse(request.getDateOfBirth());
        }

        // Duplicate Check
        if (dob != null) {
            boolean isDuplicate = patientRepository
                    .existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndPhoneAndDateOfBirthAndIsDeletedFalse(
                            request.getFirstName(), request.getLastName(), request.getPhone(), dob);
            if (isDuplicate) {
                throw new DuplicatePatientException("A patient with this exact Name, Phone, and DOB already exists.");
            }
        }

        Patient patient = new Patient();
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setDateOfBirth(dob);
        patient.setAge(request.getAge());
        patient.setAddress(request.getAddress());
        patient.setIdProof(request.getIdProof());
        patient.setCreatedBy(createdBy);

        // Save initially to generate primary key (ID)
        // PatientID must be unique, nullable = false, we temporarily set it to a dummy
        // or allow it if the DB allows.
        // Wait, MySQL prevents null on patient_id if nullable=false.
        // We will generate the ID BEFORE saving using a lock or UUID if we want it
        // completely safe.
        // Let's use a secure atomic sequence approach. Since this is an offline LAN
        // app, we can just use the DB auto increment.
        // We must set a temporary unique ID, then update it, or use UUIDs.
        // A better approach for readable IDs: "PT-" +
        // UUID.randomUUID().toString().substring(0,8).toUpperCase()
        // Or generate it from timestamp: "PT" + System.currentTimeMillis()
        // Let's use UUID for Phase 1 to guarantee zero DB race conditions on id save.

        patient.setPatientId("PT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Patient saved = patientRepository.save(patient);

        // Optional: you can re-update it to PT-{saved.getId()} if you prefer
        // sequential:
        saved.setPatientId("PT-" + String.format("%05d", saved.getId()));
        saved = patientRepository.save(saved);

        return new PatientDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PatientDto> getAllPatients() {
        return patientRepository
                .findByIsDeletedFalseAndRegistrationTypeOrderByRegistrationDateDesc(
                        com.hms.backend.model.RegistrationType.IPD)
                .stream()
                .map(PatientDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PatientDto getPatientById(String patientId) {
        Patient patient = patientRepository.findByPatientIdAndIsDeletedFalse(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        return new PatientDto(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientDto> searchPatients(String keyword) {
        return patientRepository.searchPatients(keyword).stream()
                .map(PatientDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PatientDto updatePatient(String patientId, PatientUpdateRequest request) {
        Patient patient = patientRepository.findByPatientIdAndIsDeletedFalse(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (request.getDateOfBirth() == null && request.getAge() == null) {
            throw new IllegalArgumentException("Either Date of Birth or Age must be provided.");
        }

        LocalDate dob = null;
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().isBlank()) {
            dob = LocalDate.parse(request.getDateOfBirth());
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());
        patient.setDateOfBirth(dob);
        patient.setAge(request.getAge());
        patient.setAddress(request.getAddress());
        patient.setIdProof(request.getIdProof());

        return new PatientDto(patientRepository.save(patient));
    }

    @Transactional
    public void mergePatients(String sourcePatientId, String targetPatientId) {
        if (sourcePatientId.equals(targetPatientId)) {
            throw new IllegalArgumentException("Cannot merge a patient into themselves.");
        }

        Patient source = patientRepository.findByPatientIdAndIsDeletedFalse(sourcePatientId)
                .orElseThrow(() -> new IllegalArgumentException("Source patient not found"));
        Patient target = patientRepository.findByPatientIdAndIsDeletedFalse(targetPatientId)
                .orElseThrow(() -> new IllegalArgumentException("Target patient not found"));

        source.setDeleted(true);
        source.setMergedIntoId(target.getId());
        patientRepository.save(source);

        // Note: In Phase 2, we must also migrate all Visit/Billing records from Source
        // to Target!
    }

    @Transactional
    public void softDeletePatient(String patientId) {
        Patient patient = patientRepository.findByPatientIdAndIsDeletedFalse(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        patient.setDeleted(true);
        patientRepository.save(patient);
    }
}
