package com.hms.backend.repository;

import com.hms.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByPatientIdAndIsDeletedFalse(String patientId);

    Optional<Patient> findByIdAndIsDeletedFalse(Long id);

    List<Patient> findByIsDeletedFalseOrderByRegistrationDateDesc();

    @Query("SELECT p FROM Patient p WHERE p.isDeleted = false AND " +
            "(LOWER(p.patientId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(CONCAT(p.firstName, ' ', p.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Patient> searchPatients(@Param("keyword") String keyword);

    boolean existsByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndPhoneAndDateOfBirthAndIsDeletedFalse(
            String firstName, String lastName, String phone, LocalDate dateOfBirth);
}
