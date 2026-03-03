package com.hms.backend.service;

import com.hms.backend.dto.*;
import com.hms.backend.event.AuditEvent;
import com.hms.backend.model.*;
import com.hms.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClinicalService {

        @Autowired
        private ClinicalNoteRepository clinicalNoteRepository;

        @Autowired
        private DiagnosisRepository diagnosisRepository;

        @Autowired
        private OpdVisitRepository opdVisitRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ApplicationEventPublisher eventPublisher;

        @jakarta.annotation.PostConstruct
        public void migrateOldVisits() {
                List<OpdVisit> visits = opdVisitRepository.findAll();
                boolean updated = false;

                for (OpdVisit visit : visits) {
                        if (visit.getVisitStatus() == null) {
                                if (visit.getBill() != null) {
                                        visit.setVisitStatus(ClinicalVisitStatus.COMPLETED);
                                } else {
                                        visit.setVisitStatus(ClinicalVisitStatus.CREATED);
                                }
                                opdVisitRepository.save(visit);
                                updated = true;
                        }
                }
                if (updated) {
                        System.out.println("Data migration completed for existing visits.");
                }
        }

        private User getCurrentUser() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));
        }

        @Transactional
        public ClinicalNoteDto addClinicalNote(String visitId, CreateClinicalNoteRequest request) {
                OpdVisit visit = opdVisitRepository.findByVisitId(visitId)
                                .orElseThrow(() -> new RuntimeException("Visit not found"));

                if (visit.getVisitStatus() == ClinicalVisitStatus.CLOSED) {
                        throw new RuntimeException("Cannot add notes to a CLOSED visit");
                }

                User currentUser = getCurrentUser();

                Integer version = 1;
                String oldValue = null;
                Optional<ClinicalNote> existingNoteOpt = clinicalNoteRepository
                                .findByVisitIdAndNoteTypeAndIsCurrentTrue(visit.getId(), request.getNoteType());

                if (existingNoteOpt.isPresent()) {
                        ClinicalNote existingNote = existingNoteOpt.get();
                        existingNote.setCurrent(false);
                        clinicalNoteRepository.save(existingNote);
                        version = existingNote.getVersionNumber() + 1;
                        oldValue = existingNote.getNoteContent();
                }

                ClinicalNote note = ClinicalNote.builder()
                                .visit(visit)
                                .noteType(request.getNoteType())
                                .noteContent(request.getNoteContent())
                                .versionNumber(version)
                                .createdByUserId(currentUser.getId())
                                .isCurrent(true)
                                .build();

                note = clinicalNoteRepository.save(note);

                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("CLINICAL_NOTE")
                                .entityId(note.getId().toString())
                                .actionType(version == 1 ? "CREATE" : "UPDATE")
                                .oldValue(oldValue)
                                .newValue(request.getNoteContent())
                                .performedByUserId(currentUser.getUsername())
                                .reason(request.getNoteType().name() + " note " + (version == 1 ? "created" : "updated")
                                                + " for "
                                                + visitId)
                                .build());

                return mapToClinicalNoteDto(note);
        }

        public List<ClinicalNoteDto> getNotesForVisit(String visitId) {
                OpdVisit visit = opdVisitRepository.findByVisitId(visitId)
                                .orElseThrow(() -> new RuntimeException("Visit not found"));

                return clinicalNoteRepository.findByVisitIdOrderByCreatedAtDesc(visit.getId())
                                .stream()
                                .map(this::mapToClinicalNoteDto)
                                .collect(Collectors.toList());
        }

        @Transactional
        public DiagnosisDto addDiagnosis(String visitId, CreateDiagnosisRequest request) {
                OpdVisit visit = opdVisitRepository.findByVisitId(visitId)
                                .orElseThrow(() -> new RuntimeException("Visit not found"));

                if (visit.getVisitStatus() == ClinicalVisitStatus.CLOSED) {
                        throw new RuntimeException("Cannot add diagnosis to a CLOSED visit");
                }

                User currentUser = getCurrentUser();

                Diagnosis diagnosis = Diagnosis.builder()
                                .visit(visit)
                                .diagnosisCode(request.getDiagnosisCode())
                                .diagnosisDescription(request.getDiagnosisDescription())
                                .isPrimary(request.getIsPrimary())
                                .createdBy(currentUser.getId())
                                .build();

                diagnosis = diagnosisRepository.save(diagnosis);

                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("DIAGNOSIS")
                                .entityId(diagnosis.getId().toString())
                                .actionType("CREATE")
                                .newValue("Code: " + diagnosis.getDiagnosisCode() + ", Desc: "
                                                + diagnosis.getDiagnosisDescription())
                                .performedByUserId(currentUser.getUsername())
                                .reason("Added diagnosis for visit " + visitId)
                                .build());

                return mapToDiagnosisDto(diagnosis);
        }

        public List<DiagnosisDto> getDiagnosesForVisit(String visitId) {
                OpdVisit visit = opdVisitRepository.findByVisitId(visitId)
                                .orElseThrow(() -> new RuntimeException("Visit not found"));

                return diagnosisRepository.findByVisitId(visit.getId())
                                .stream()
                                .map(this::mapToDiagnosisDto)
                                .collect(Collectors.toList());
        }

        @Transactional
        public void updateVisitStatus(String visitId, UpdateVisitStatusRequest request) {
                OpdVisit visit = opdVisitRepository.findByVisitId(visitId)
                                .orElseThrow(() -> new RuntimeException("Visit not found"));

                ClinicalVisitStatus oldStatus = visit.getVisitStatus();
                ClinicalVisitStatus newStatus = request.getStatus();

                if (oldStatus == newStatus) {
                        return;
                }

                visit.setVisitStatus(newStatus);
                opdVisitRepository.save(visit);

                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("VISIT")
                                .entityId(visitId)
                                .actionType("UPDATE_STATUS")
                                .oldValue(oldStatus.name())
                                .newValue(newStatus.name())
                                .performedByUserId(getCurrentUser().getUsername())
                                .reason("Status changed from " + oldStatus + " to " + newStatus)
                                .build());
        }

        private ClinicalNoteDto mapToClinicalNoteDto(ClinicalNote note) {
                return ClinicalNoteDto.builder()
                                .id(note.getId())
                                .visitId(note.getVisit().getVisitId())
                                .noteType(note.getNoteType())
                                .noteContent(note.getNoteContent())
                                .versionNumber(note.getVersionNumber())
                                .createdByUserId(note.getCreatedByUserId())
                                .createdAt(note.getCreatedAt())
                                .isCurrent(note.isCurrent())
                                .build();
        }

        private DiagnosisDto mapToDiagnosisDto(Diagnosis diagnosis) {
                return DiagnosisDto.builder()
                                .id(diagnosis.getId())
                                .visitId(diagnosis.getVisit().getVisitId())
                                .diagnosisCode(diagnosis.getDiagnosisCode())
                                .diagnosisDescription(diagnosis.getDiagnosisDescription())
                                .isPrimary(diagnosis.isPrimary())
                                .createdBy(diagnosis.getCreatedBy())
                                .createdAt(diagnosis.getCreatedAt())
                                .build();
        }
}
