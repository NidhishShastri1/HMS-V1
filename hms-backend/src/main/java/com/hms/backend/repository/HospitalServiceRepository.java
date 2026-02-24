package com.hms.backend.repository;

import com.hms.backend.model.HospitalService;
import com.hms.backend.model.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalServiceRepository extends JpaRepository<HospitalService, Long> {

    Optional<HospitalService> findByServiceId(String serviceId);

    boolean existsByServiceNameIgnoreCaseAndCategory(String serviceName, ServiceCategory category);

    List<HospitalService> findAllByOrderByCategoryAscServiceNameAsc();

    List<HospitalService> findByIsActiveTrueOrderByCategoryAscServiceNameAsc();
}
