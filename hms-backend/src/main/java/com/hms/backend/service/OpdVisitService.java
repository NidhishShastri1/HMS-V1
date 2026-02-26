package com.hms.backend.service;

import com.hms.backend.dto.*;
import com.hms.backend.event.AuditEvent;
import com.hms.backend.model.*;
import com.hms.backend.repository.*;
import com.hms.backend.util.AmountInWordsConverter;
import com.hms.backend.util.RecordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OpdVisitService {

        @Autowired
        private OpdVisitRepository opdVisitRepository;

        @Autowired
        private PatientRepository patientRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private BillRepository billRepository;

        @Autowired
        private BillItemRepository billItemRepository;

        @Autowired
        private PaymentRepository paymentRepository;

        @Autowired
        private HospitalServiceRepository hospitalServiceRepository;

        @Autowired
        private ApplicationEventPublisher eventPublisher;

        @Autowired
        private BillAdjustmentRepository billAdjustmentRepository;

        @Autowired
        private RecordHasher recordHasher;

        @Autowired
        private SecurityService securityService;

        private User getCurrentUser() {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("User not found"));
        }

        private String generateVisitId() {
                return "V-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        private String generateBillId() {
                return "B-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        private String generateReceiptId() {
                return "RCPT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        @Transactional
        public BillDto createQuickOpdBilling(QuickOpdBillingRequest request) {
                User currentUser = getCurrentUser();

                // 1. Create/Save Patient record (Tagged as OPD_ONLY)
                Patient patient = new Patient();
                patient.setFirstName(request.getFirstName());
                patient.setLastName(request.getLastName());
                patient.setPhone(request.getPhone());
                patient.setAge(request.getAge());
                patient.setGender(request.getGender());
                patient.setAddress(request.getAddress());
                patient.setRegistrationType(RegistrationType.OPD_ONLY);
                patient.setCreatedBy(currentUser.getUsername());

                // Generate temporary ID to satisfy non-null constraints if any,
                // then refine after getting the DB primary key.
                patient.setPatientId("PT-TEMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                Patient savedPatient = patientRepository.save(patient);

                // Final internal UHID format: PT-O-00001
                savedPatient.setPatientId("PT-O-" + String.format("%05d", savedPatient.getId()));
                savedPatient = patientRepository.save(savedPatient);

                // 2. Auto-Create OPD Visit
                OpdVisit visit = OpdVisit.builder()
                                .visitId(generateVisitId())
                                .patient(savedPatient)
                                .assignedDoctor(request.getAssignedDoctor())
                                .department(request.getDepartment())
                                .visitCategory(request.getVisitCategory())
                                .visitDateTime(LocalDateTime.now())
                                .status(VisitStatus.OPEN)
                                .notes(request.getNotes())
                                .createdBy(currentUser)
                                .build();

                OpdVisit savedVisit = opdVisitRepository.save(visit);

                // 3. Auto-Create Bill attached to Visit
                Bill bill = Bill.builder()
                                .billId(generateBillId())
                                .visit(savedVisit)
                                .status(BillStatus.DRAFT)
                                .subTotal(BigDecimal.ZERO)
                                .tax(BigDecimal.ZERO)
                                .discount(BigDecimal.ZERO)
                                .grandTotal(BigDecimal.ZERO)
                                .createdBy(currentUser)
                                .build();

                Bill savedBill = billRepository.save(bill);

                // --- PHASE 2: Integrity Hashing ---
                updateBillHash(savedBill);

                savedVisit.setBill(savedBill);
                opdVisitRepository.save(savedVisit);

                // --- PHASE 2: AUDIT ENGINE (Decoupled, Passive) ---
                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("BILL")
                                .entityId(savedBill.getBillId())
                                .actionType("CREATE")
                                .newValue("Quick Billing for: " + savedPatient.getFirstName() + " "
                                                + savedPatient.getLastName())
                                .performedByUserId(currentUser.getUsername())
                                .build());

                return mapToBillDto(savedBill, new java.util.ArrayList<>(), new java.util.ArrayList<>());
        }

        @Transactional
        public OpdVisitDto createOpdVisit(CreateOpdVisitRequest request) {
                Patient patient = patientRepository.findByPatientIdAndIsDeletedFalse(request.getPatientId())
                                .orElseThrow(() -> new RuntimeException("Patient not found"));

                User currentUser = getCurrentUser();

                OpdVisit visit = OpdVisit.builder()
                                .visitId(generateVisitId())
                                .patient(patient)
                                .assignedDoctor(request.getAssignedDoctor())
                                .department(request.getDepartment())
                                .visitCategory(request.getVisitCategory())
                                .visitDateTime(LocalDateTime.now()) // Force server time
                                .status(VisitStatus.OPEN)
                                .notes(request.getNotes())
                                .createdBy(currentUser)
                                .build();

                OpdVisit savedVisit = opdVisitRepository.save(visit);

                // Auto-generate draft bill
                Bill bill = Bill.builder()
                                .billId(generateBillId())
                                .visit(savedVisit)
                                .subTotal(BigDecimal.ZERO)
                                .tax(BigDecimal.ZERO)
                                .discount(BigDecimal.ZERO)
                                .grandTotal(BigDecimal.ZERO)
                                .amountInWords("Zero Rupees Only")
                                .status(BillStatus.DRAFT)
                                .paymentStatus(PaymentStatus.UNPAID)
                                .createdBy(currentUser)
                                .build();

                Bill savedBill = billRepository.save(bill);
                updateBillHash(savedBill);

                return mapToOpdVisitDto(savedVisit, savedBill);
        }

        public List<OpdVisitDto> getAllVisits() {
                return opdVisitRepository.findAll().stream().map(v -> mapToOpdVisitDto(v, v.getBill()))
                                .collect(Collectors.toList());
        }

        public OpdVisitDto getVisitById(String visitId) {
                OpdVisit v = opdVisitRepository.findByVisitId(visitId)
                                .orElseThrow(() -> new RuntimeException("Visit not found"));
                return mapToOpdVisitDto(v, v.getBill());
        }

        @Transactional
        public BillDto addServiceToBill(String billId, AddServiceToBillRequest request) {
                Bill bill = billRepository.findByBillId(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                if (bill.getStatus() != BillStatus.DRAFT) {
                        throw new RuntimeException(
                                        "Cannot add services to a " + bill.getStatus()
                                                        + " bill. Revert to DRAFT or create new visit.");
                }

                HospitalService service = hospitalServiceRepository.findByServiceId(request.getServiceId())
                                .orElseThrow(() -> new RuntimeException("Service not found"));

                if (!service.isActive()) {
                        throw new RuntimeException("Service is not active");
                }

                User currentUser = getCurrentUser();
                BigDecimal unitPrice = service.getDefaultPrice();

                if (request.getOverriddenPrice() != null) {
                        if (!currentUser.getRole().name().equals("ADMIN")) {
                                throw new RuntimeException("Only Admins can override service prices");
                        }
                        unitPrice = request.getOverriddenPrice();
                }

                BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

                BillItem item = BillItem.builder()
                                .bill(bill)
                                .service(service)
                                .quantity(request.getQuantity())
                                .unitPrice(unitPrice)
                                .totalAmount(total)
                                .build();

                billItemRepository.save(item);

                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("BILL")
                                .entityId(billId)
                                .actionType("UPDATE_ADD_SERVICE")
                                .newValue("Added service: " + service.getServiceName())
                                .performedByUserId(currentUser.getUsername())
                                .build());

                return recalculateBill(bill);
        }

        @Transactional
        public BillDto adjustBill(String billId, BillAdjustmentRequest request) {
                Bill bill = billRepository.findByBillId(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                if (bill.getStatus() == BillStatus.CANCELLED || bill.getStatus() == BillStatus.LOCKED) {
                        throw new RuntimeException("Cannot adjust a cancelled or locked bill");
                }

                User currentUser = getCurrentUser();
                BigDecimal oldTotal = bill.getGrandTotal();

                // Non-destructive: record the adjustment
                BillAdjustment adjustment = BillAdjustment.builder()
                                .bill(bill)
                                .adjustedBy(currentUser)
                                .originalAmount(oldTotal)
                                .adjustedAmount(request.getAdjustedAmount())
                                .reason(request.getReason())
                                .adjustmentType("MANUAL_OVERRIDE")
                                .build();

                billAdjustmentRepository.save(adjustment);

                // Update the bill with adjustment logic
                // For simplicity, we override the grandTotal directly as a manual correction
                bill.setGrandTotal(request.getAdjustedAmount());
                bill.setAmountInWords(AmountInWordsConverter.convert(request.getAdjustedAmount()));
                bill.setAdjusted(true);
                bill.setAdjustedAt(LocalDateTime.now());

                updateBillHash(bill);

                // Trigger Audit Log
                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("BILL")
                                .entityId(bill.getBillId())
                                .actionType("ADJUST")
                                .oldValue("Total: " + oldTotal)
                                .newValue("Total: " + request.getAdjustedAmount())
                                .performedByUserId(currentUser.getUsername())
                                .reason(request.getReason())
                                .build());

                return getBillById(billId);
        }

        @Transactional
        public BillDto removeServiceFromBill(String billId, Long itemId) {
                Bill bill = billRepository.findByBillId(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                if (bill.getStatus() != BillStatus.DRAFT) {
                        throw new RuntimeException("Cannot remove services from a " + bill.getStatus() + " bill");
                }

                BillItem item = billItemRepository.findById(itemId)
                                .orElseThrow(() -> new RuntimeException("Item not found"));

                if (!item.getBill().getId().equals(bill.getId())) {
                        throw new RuntimeException("Item does not belong to this bill");
                }

                billItemRepository.delete(item);

                // flush needed to update collection before recalculate?
                bill.getItems().remove(item);

                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("BILL")
                                .entityId(billId)
                                .actionType("UPDATE_REMOVE_SERVICE")
                                .newValue("Removed service: " + item.getService().getServiceName())
                                .performedByUserId(getCurrentUser().getUsername())
                                .build());

                return recalculateBill(bill);
        }

        @Transactional
        public BillDto cancelBill(String billId) {
                Bill bill = billRepository.findByBillId(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                if (bill.getStatus() == BillStatus.LOCKED) {
                        throw new RuntimeException("Cannot cancel a locked bill");
                }

                bill.setAdjusted(true);
                bill.setAdjustedAt(LocalDateTime.now());
                updateBillHash(bill);

                OpdVisit visit = bill.getVisit();
                visit.setStatus(VisitStatus.CANCELLED);
                opdVisitRepository.save(visit);

                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("BILL")
                                .entityId(billId)
                                .actionType("CANCEL")
                                .newValue("Bill cancelled")
                                .reason("Manual cancellation")
                                .performedByUserId(getCurrentUser().getUsername())
                                .build());

                return mapToBillDto(bill, bill.getItems(), bill.getPayments());
        }

        @Transactional
        public BillDto recalculateBill(Bill bill) {
                List<BillItem> items = billItemRepository.findByBill_BillId(bill.getBillId());

                BigDecimal subTotal = items.stream()
                                .map(BillItem::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                bill.setSubTotal(subTotal);

                // Simple Tax calculation logic: 5% tax if subtotal > 0
                BigDecimal taxRate = new BigDecimal("0.05");
                BigDecimal calculatedTax = subTotal.multiply(taxRate).setScale(2, java.math.RoundingMode.HALF_UP);
                bill.setTax(calculatedTax);

                bill.setGrandTotal(
                                subTotal.add(bill.getTax()).subtract(bill.getDiscount()).setScale(2,
                                                java.math.RoundingMode.HALF_UP));

                bill.setAmountInWords(AmountInWordsConverter.convert(bill.getGrandTotal()));

                updateBillHash(bill);
                return mapToBillDto(bill, items, paymentRepository.findByBill_BillId(bill.getBillId()));
        }

        public BillDto getBillById(String billId) {
                Bill bill = billRepository.findByBillId(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                validateBillIntegrity(bill);

                return mapToBillDto(bill, bill.getItems(), bill.getPayments());
        }

        @Transactional
        public BillDto addPayment(String billId, CreatePaymentRequest request) {
                Bill bill = billRepository.findByBillId(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                if (bill.getStatus() == BillStatus.CANCELLED || bill.getStatus() == BillStatus.LOCKED) {
                        throw new RuntimeException("Cannot make payment on a cancelled or locked bill");
                }

                User currentUser = getCurrentUser();

                Payment payment = Payment.builder()
                                .receiptId(generateReceiptId())
                                .bill(bill)
                                .paymentMode(request.getPaymentMode())
                                .amount(request.getAmount())
                                .paymentDateTime(LocalDateTime.now()) // Force server time
                                .receivedBy(currentUser)
                                .build();

                Payment savedPayment = paymentRepository.save(payment);
                updatePaymentHash(savedPayment);

                List<Payment> allPayments = paymentRepository.findByBill_BillId(billId);

                BigDecimal totalPaid = allPayments.stream()
                                .map(Payment::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalPaid.compareTo(bill.getGrandTotal()) >= 0) {
                        bill.setPaymentStatus(PaymentStatus.PAID);
                        bill.setStatus(BillStatus.PAID);
                        bill.setAmountInWords(AmountInWordsConverter.convert(bill.getGrandTotal()));
                } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
                        bill.setPaymentStatus(PaymentStatus.PARTIAL);
                        bill.setStatus(BillStatus.PAYMENT_PENDING);
                }

                updateBillHash(bill);

                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("BILL")
                                .entityId(billId)
                                .actionType("PAYMENT")
                                .newValue("Amount: " + request.getAmount() + " via " + request.getPaymentMode())
                                .performedByUserId(currentUser.getUsername())
                                .build());

                return mapToBillDto(bill, billItemRepository.findByBill_BillId(billId), allPayments);
        }

        @Transactional
        public BillDto finalizeBill(String billId) {
                Bill bill = billRepository.findByBillId(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                if (bill.getStatus() != BillStatus.PAID) {
                        throw new RuntimeException("Bill must be in PAID status to be locked/finalized");
                }

                List<Payment> payments = paymentRepository.findByBill_BillId(billId);

                bill.setStatus(BillStatus.LOCKED);
                bill.setPaymentStatus(PaymentStatus.PAID);

                updateBillHash(bill);

                OpdVisit visit = bill.getVisit();
                visit.setStatus(VisitStatus.BILLED);
                opdVisitRepository.save(visit);

                eventPublisher.publishEvent(AuditEvent.builder()
                                .entityName("BILL")
                                .entityId(billId)
                                .actionType("FINALIZE")
                                .newValue("Bill finalized and locked")
                                .performedByUserId(getCurrentUser().getUsername())
                                .build());

                return mapToBillDto(bill, billItemRepository.findByBill_BillId(billId), payments);
        }

        private OpdVisitDto mapToOpdVisitDto(OpdVisit visit, Bill bill) {
                return OpdVisitDto.builder()
                                .visitId(visit.getVisitId())
                                .patientId(visit.getPatient().getPatientId())
                                .patientName(visit.getPatient().getFirstName() + " " + visit.getPatient().getLastName())
                                .assignedDoctor(visit.getAssignedDoctor())
                                .department(visit.getDepartment())
                                .visitCategory(visit.getVisitCategory())
                                .visitDateTime(visit.getVisitDateTime())
                                .status(visit.getStatus())
                                .notes(visit.getNotes())
                                .billId(bill != null ? bill.getBillId() : null)
                                .createdAt(visit.getCreatedAt())
                                .createdBy(visit.getCreatedBy() != null ? visit.getCreatedBy().getUsername() : "SYSTEM")
                                .build();
        }

        private BillDto mapToBillDto(Bill bill, List<BillItem> items, List<Payment> payments) {
                return BillDto.builder()
                                .billId(bill.getBillId())
                                .visitId(bill.getVisit().getVisitId())
                                .patientId(bill.getVisit().getPatient().getPatientId())
                                .patientName(
                                                bill.getVisit().getPatient().getFirstName() + " "
                                                                + bill.getVisit().getPatient().getLastName())
                                .age(bill.getVisit().getPatient().getAge())
                                .gender(bill.getVisit().getPatient().getGender())
                                .phone(bill.getVisit().getPatient().getPhone())
                                .subTotal(bill.getSubTotal())
                                .tax(bill.getTax())
                                .discount(bill.getDiscount())
                                .grandTotal(bill.getGrandTotal())
                                .status(bill.getStatus())
                                .amountInWords(bill.getAmountInWords())
                                .paymentStatus(bill.getPaymentStatus())
                                .items(items.stream().map(this::mapToBillItemDto).collect(Collectors.toList()))
                                .payments(payments.stream().map(this::mapToPaymentDto).collect(Collectors.toList()))
                                .createdBy(bill.getCreatedBy() != null ? bill.getCreatedBy().getUsername() : "SYSTEM")
                                .createdAt(bill.getCreatedAt())
                                .updatedAt(bill.getUpdatedAt())
                                .isAdjusted(bill.isAdjusted())
                                .adjustedAt(bill.getAdjustedAt())
                                .build();
        }

        private BillItemDto mapToBillItemDto(BillItem item) {
                return BillItemDto.builder()
                                .id(item.getId())
                                .serviceId(item.getService().getServiceId())
                                .serviceName(item.getService().getServiceName())
                                .quantity(item.getQuantity())
                                .unitPrice(item.getUnitPrice())
                                .totalAmount(item.getTotalAmount())
                                .build();
        }

        private PaymentDto mapToPaymentDto(Payment payment) {
                return PaymentDto.builder()
                                .receiptId(payment.getReceiptId())
                                .paymentMode(payment.getPaymentMode())
                                .amount(payment.getAmount())
                                .paymentDateTime(payment.getPaymentDateTime())
                                .receivedBy(payment.getReceivedBy() != null ? payment.getReceivedBy().getUsername()
                                                : "SYSTEM")
                                .build();
        }

        private void updateBillHash(Bill bill) {
                if (bill.getPreviousHash() == null) {
                        String prevHash = billRepository.findTopByOrderByIdDesc()
                                        .filter(b -> !b.getBillId().equals(bill.getBillId()))
                                        .map(Bill::getRecordHash).orElse("ROOT");
                        bill.setPreviousHash(prevHash);
                }
                bill.setRecordHash(recordHasher.computeBillHash(bill, bill.getPreviousHash()));
                billRepository.save(bill);
        }

        private void validateBillIntegrity(Bill bill) {
                if (bill.getRecordHash() == null)
                        return; // Legacy record

                String currentHash = recordHasher.computeBillHash(bill, bill.getPreviousHash());
                if (!currentHash.equals(bill.getRecordHash())) {
                        securityService.logIntegrityViolation("BILL", bill.getBillId(),
                                        "Hash mismatch. Expected: " + bill.getRecordHash() + ", Computed: "
                                                        + currentHash);
                }
        }

        private void updatePaymentHash(Payment payment) {
                if (payment.getPreviousHash() == null) {
                        String prevHash = paymentRepository.findTopByOrderByIdDesc()
                                        .filter(p -> !p.getReceiptId().equals(payment.getReceiptId()))
                                        .map(Payment::getRecordHash).orElse("ROOT");
                        payment.setPreviousHash(prevHash);
                }
                payment.setRecordHash(recordHasher.computePaymentHash(payment, payment.getPreviousHash()));
                paymentRepository.save(payment);
        }
}
