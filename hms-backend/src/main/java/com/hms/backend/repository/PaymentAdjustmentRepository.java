package com.hms.backend.repository;

import com.hms.backend.model.PaymentAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentAdjustmentRepository extends JpaRepository<PaymentAdjustment, Long> {
    List<PaymentAdjustment> findByPayment_ReceiptIdOrderByAdjustedAtDesc(String receiptId);
}
