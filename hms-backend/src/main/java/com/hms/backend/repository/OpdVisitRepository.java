package com.hms.backend.repository;

import com.hms.backend.model.OpdVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpdVisitRepository extends JpaRepository<OpdVisit, Long> {
    Optional<OpdVisit> findByVisitId(String visitId);

    List<OpdVisit> findByPatient_PatientId(String patientId);
}
