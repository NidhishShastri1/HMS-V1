package com.hms.backend.repository;

import com.hms.backend.model.HospitalService;
import com.hms.backend.model.ServicePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePriceHistoryRepository extends JpaRepository<ServicePriceHistory, Long> {

    List<ServicePriceHistory> findByServiceOrderByChangedAtDesc(HospitalService service);

    List<ServicePriceHistory> findAllByOrderByChangedAtDesc();
}
