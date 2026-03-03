package com.hms.backend.repository;

import com.hms.backend.model.IpdChargeType;
import com.hms.backend.model.IpdDailyCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IpdDailyChargeRepository extends JpaRepository<IpdDailyCharge, Long> {
    List<IpdDailyCharge> findByVisitIdOrderByChargeDateDesc(Long visitId);

    Optional<IpdDailyCharge> findByVisitIdAndChargeDateAndChargeType(Long visitId, LocalDate chargeDate,
            IpdChargeType chargeType);
}
