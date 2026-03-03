package com.hms.backend.repository;

import com.hms.backend.model.ClinicalNote;
import com.hms.backend.model.ClinicalNoteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, Long> {
    List<ClinicalNote> findByVisitIdOrderByCreatedAtDesc(Long visitId);

    Optional<ClinicalNote> findByVisitIdAndNoteTypeAndIsCurrentTrue(Long visitId, ClinicalNoteType type);
}
