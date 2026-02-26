package com.hms.backend.repository;

import com.hms.backend.model.BillAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillAdjustmentRepository extends JpaRepository<BillAdjustment, Long> {
    List<BillAdjustment> findByBill_BillIdOrderByAdjustedAtDesc(String billId);

    long countByAdjustmentType(String adjustmentType);
}
