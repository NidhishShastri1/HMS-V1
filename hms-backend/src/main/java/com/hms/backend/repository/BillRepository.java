package com.hms.backend.repository;

import com.hms.backend.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillId(String billId);

    Optional<Bill> findByVisit_VisitId(String visitId);

    Optional<Bill> findTopByOrderByIdDesc();

    long countByIsAdjustedTrue();
}
