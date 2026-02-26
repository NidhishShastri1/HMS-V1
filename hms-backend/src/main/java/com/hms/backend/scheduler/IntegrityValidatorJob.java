package com.hms.backend.scheduler;

import com.hms.backend.model.Bill;
import com.hms.backend.model.Payment;
import com.hms.backend.repository.BillRepository;
import com.hms.backend.repository.PaymentRepository;
import com.hms.backend.service.SecurityService;
import com.hms.backend.util.RecordHasher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class IntegrityValidatorJob {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RecordHasher recordHasher;

    @Autowired
    private SecurityService securityService;

    /**
     * Nightly integrity check at 3 AM.
     * Scans all bills and payments to verify hash chains.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void validateFullIntegrity() {
        log.info("Starting nightly integrity validation...");

        validateBills();
        validatePayments();

        log.info("Integrity validation completed.");
    }

    private void validateBills() {
        List<Bill> bills = billRepository.findAll();
        for (Bill bill : bills) {
            if (bill.getRecordHash() == null)
                continue;

            String computedHash = recordHasher.computeBillHash(bill, bill.getPreviousHash());
            if (!computedHash.equals(bill.getRecordHash())) {
                securityService.logIntegrityViolation("BILL", bill.getBillId(),
                        "NIGHTLY_SCAN: Hash mismatch detected. Data may have been tampered with.");
            }
        }
    }

    private void validatePayments() {
        List<Payment> payments = paymentRepository.findAll();
        for (Payment payment : payments) {
            if (payment.getRecordHash() == null)
                continue;

            String computedHash = recordHasher.computePaymentHash(payment, payment.getPreviousHash());
            if (!computedHash.equals(payment.getRecordHash())) {
                securityService.logIntegrityViolation("PAYMENT", payment.getReceiptId(),
                        "NIGHTLY_SCAN: Hash mismatch detected. Data may have been tampered with.");
            }
        }
    }
}
