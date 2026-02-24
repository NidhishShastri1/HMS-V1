package com.hms.backend.service;

import com.hms.backend.dto.AddServiceToBillRequest;
import com.hms.backend.dto.BillDto;
import com.hms.backend.dto.CreatePaymentRequest;
import com.hms.backend.dto.OpdVisitDto;
import com.hms.backend.model.*;
import com.hms.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OpdVisitServiceTest {

    @Mock
    private OpdVisitRepository opdVisitRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private BillItemRepository billItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private HospitalServiceRepository hospitalServiceRepository;

    @InjectMocks
    private OpdVisitService opdVisitService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        User user = new User();
        user.setUsername("testuser");
        user.setRole(Role.ADMIN);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Add default createdBy to OpdVisit and Bill in tests
        lenient().doAnswer(invocation -> {
            OpdVisit visit = invocation.getArgument(0);
            if (visit.getCreatedBy() == null)
                visit.setCreatedBy(user);
            return visit;
        }).when(opdVisitRepository).save(any(OpdVisit.class));

        lenient().doAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            if (bill.getCreatedBy() == null)
                bill.setCreatedBy(user);
            return bill;
        }).when(billRepository).save(any(Bill.class));
    }

    @Test
    void testBillCalculation() {
        Bill bill = new Bill();
        bill.setBillId("B1");
        bill.setStatus(BillStatus.DRAFT);
        bill.setTax(BigDecimal.ZERO);
        bill.setDiscount(BigDecimal.ZERO);
        bill.setVisit(new OpdVisit());
        bill.getVisit().setPatient(new Patient());
        bill.getVisit().getPatient().setFirstName("John");
        bill.getVisit().getPatient().setLastName("Doe");

        HospitalService service = new HospitalService();
        service.setServiceId("S1");
        service.setServiceName("Consultation");
        service.setDefaultPrice(new BigDecimal("500.00"));
        service.setActive(true);

        when(billRepository.findByBillId("B1")).thenReturn(Optional.of(bill));
        when(hospitalServiceRepository.findByServiceId("S1")).thenReturn(Optional.of(service));

        AddServiceToBillRequest request = new AddServiceToBillRequest();
        request.setServiceId("S1");
        request.setQuantity(2);

        BillItem item = BillItem.builder()
                .bill(bill)
                .service(service)
                .quantity(2)
                .unitPrice(new BigDecimal("500.00"))
                .totalAmount(new BigDecimal("1000.00"))
                .build();

        ArrayList<BillItem> items = new ArrayList<>();
        items.add(item);
        when(billItemRepository.findByBill_BillId("B1")).thenReturn(items);

        BillDto result = opdVisitService.addServiceToBill("B1", request);

        assertEquals(new BigDecimal("1000.00"), result.getSubTotal());
        assertEquals(new BigDecimal("50.00"), result.getTax());
        assertEquals(new BigDecimal("1050.00"), result.getGrandTotal());
    }

    @Test
    void testPartialPayment() {
        Bill bill = new Bill();
        bill.setBillId("B1");
        bill.setGrandTotal(new BigDecimal("1000.00"));
        bill.setStatus(BillStatus.DRAFT);
        bill.setVisit(new OpdVisit());
        bill.getVisit().setPatient(new Patient());
        bill.getVisit().getPatient().setFirstName("John");
        bill.getVisit().getPatient().setLastName("Doe");

        when(billRepository.findByBillId("B1")).thenReturn(Optional.of(bill));

        CreatePaymentRequest paymentReq = new CreatePaymentRequest();
        paymentReq.setAmount(new BigDecimal("400.00"));
        paymentReq.setPaymentMode(PaymentMode.CASH);

        ArrayList<Payment> payments = new ArrayList<>();
        when(paymentRepository.findByBill_BillId("B1")).thenReturn(payments);

        BillDto result = opdVisitService.addPayment("B1", paymentReq);

        assertEquals(PaymentStatus.PARTIAL, result.getPaymentStatus());
    }

    @Test
    void testLockingLogic() {
        Bill bill = new Bill();
        bill.setBillId("B1");
        bill.setStatus(BillStatus.LOCKED);

        when(billRepository.findByBillId("B1")).thenReturn(Optional.of(bill));

        AddServiceToBillRequest request = new AddServiceToBillRequest();
        request.setServiceId("S1");

        assertThrows(RuntimeException.class, () -> opdVisitService.addServiceToBill("B1", request));
    }

    @Test
    void testServiceRemovalRules() {
        Bill bill = new Bill();
        bill.setBillId("B1");
        bill.setStatus(BillStatus.LOCKED);

        when(billRepository.findByBillId("B1")).thenReturn(Optional.of(bill));

        assertThrows(RuntimeException.class, () -> opdVisitService.removeServiceFromBill("B1", 1L));
    }

    @Test
    void testCreateOpdVisit() {
        Patient patient = new Patient();
        patient.setPatientId("P1");
        patient.setFirstName("John");
        patient.setLastName("Doe");

        when(patientRepository.findByPatientIdAndIsDeletedFalse("P1")).thenReturn(Optional.of(patient));
        when(opdVisitRepository.save(any(OpdVisit.class))).thenAnswer(i -> i.getArgument(0));
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));

        com.hms.backend.dto.CreateOpdVisitRequest request = new com.hms.backend.dto.CreateOpdVisitRequest();
        request.setPatientId("P1");
        request.setAssignedDoctor("Dr. Smith");
        request.setDepartment("Cardiology");
        request.setVisitCategory(VisitCategory.CASH);

        OpdVisitDto result = opdVisitService.createOpdVisit(request);

        assertNotNull(result.getVisitId());
        assertEquals("P1", result.getPatientId());
        assertEquals("Cardiology", result.getDepartment());
        assertEquals(VisitCategory.CASH, result.getVisitCategory());
    }

    @Test
    void testPriceTamperingBlocked() {
        Bill bill = new Bill();
        bill.setBillId("B1");
        bill.setStatus(BillStatus.DRAFT);
        when(billRepository.findByBillId("B1")).thenReturn(Optional.of(bill));

        HospitalService service = new HospitalService();
        service.setServiceId("S1");
        service.setActive(true);
        service.setDefaultPrice(new BigDecimal("500.00"));
        when(hospitalServiceRepository.findByServiceId("S1")).thenReturn(Optional.of(service));

        // Create a non-admin user
        User receptionist = new User();
        receptionist.setUsername("reception");
        receptionist.setRole(Role.RECEPTION);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(receptionist));

        AddServiceToBillRequest request = new AddServiceToBillRequest();
        request.setServiceId("S1");
        request.setOverriddenPrice(new BigDecimal("100.00")); // Attempt to lower price

        assertThrows(RuntimeException.class, () -> opdVisitService.addServiceToBill("B1", request));
    }

    @Test
    void testCreateQuickOpdBilling() {
        User user = new User();
        user.setUsername("testuser");
        user.setRole(Role.ADMIN);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        when(patientRepository.save(any(Patient.class))).thenAnswer(i -> {
            Patient p = i.getArgument(0);
            if (p.getId() == null)
                p.setId(1L);
            return p;
        });

        when(opdVisitRepository.save(any(OpdVisit.class))).thenAnswer(i -> i.getArgument(0));
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));

        com.hms.backend.dto.QuickOpdBillingRequest request = new com.hms.backend.dto.QuickOpdBillingRequest();
        request.setFirstName("Quick");
        request.setLastName("Patient");
        request.setPhone("9988776655");
        request.setAge(30);
        request.setGender("Male");
        request.setAssignedDoctor("Dr. Quick");
        request.setDepartment("Emergency");
        request.setVisitCategory(VisitCategory.CASH);

        BillDto result = opdVisitService.createQuickOpdBilling(request);

        assertNotNull(result);
        assertEquals("Quick Patient", result.getPatientName());
        assertEquals(BillStatus.DRAFT, result.getStatus());
        assertTrue(result.getPatientId().startsWith("PT-O-"));
    }
}
