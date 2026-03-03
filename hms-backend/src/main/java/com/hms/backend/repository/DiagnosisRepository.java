package com.hms.backend.repository;

import com.hms.backend.model.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    List<Diagnosis> findByVisitId(Long visitId);
}
