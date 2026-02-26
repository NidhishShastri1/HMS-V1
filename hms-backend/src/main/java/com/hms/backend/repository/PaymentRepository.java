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

    Optional<Payment> findTopByOrderByIdDesc();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDateTime >= :startOfDate")
    java.math.BigDecimal sumAmountByPaymentDateTimeAfter(java.time.LocalDateTime startOfDate);

    @org.springframework.data.jpa.repository.Query("SELECT p.paymentMode, SUM(p.amount) FROM Payment p WHERE p.paymentDateTime >= :startOfDate GROUP BY p.paymentMode")
    List<Object[]> sumAmountGroupByPaymentModeAfter(java.time.LocalDateTime startOfDate);
}
