package com.hms.backend.repository;

import com.hms.backend.model.BedAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BedAllocationRepository extends JpaRepository<BedAllocation, Long> {
    List<BedAllocation> findByVisitIdOrderByAllocatedAtDesc(Long visitId);

    Optional<BedAllocation> findTopByBedNumberAndReleasedAtIsNull(String bedNumber);

    Optional<BedAllocation> findTopByVisitIdAndReleasedAtIsNull(Long visitId);
}
