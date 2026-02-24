package com.hms.backend.repository;

import com.hms.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReceiptId(String receiptId);

    List<Payment> findByBill_BillId(String billId);
}
