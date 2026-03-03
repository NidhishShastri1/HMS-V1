package com.hms.backend.service;

import com.hms.backend.dto.*;
import com.hms.backend.event.AuditEvent;
import com.hms.backend.model.*;
import com.hms.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IpdService {

    @Autowired
    private OpdVisitRepository opdVisitRepository;

    @Autowired
    private BedAllocationRepository bedAllocationRepository;

    @Autowired
    private IpdDailyChargeRepository ipdDailyChargeRepository;

    @Autowired
    private HospitalServiceRepository hospitalServiceRepository;

    @Autowired
    private BillItemRepository billItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private OpdVisitService opdVisitService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private OpdVisit getIpVisit(String visitId) {
        OpdVisit visit = opdVisitRepository.findByVisitId(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found"));
        if (visit.getPatient().getRegistrationType() != RegistrationType.IPD) {
            throw new RuntimeException("This operation is only allowed for IPD visits");
        }
        return visit;
    }

    @Transactional
    public BedAllocationDto allocateBed(String visitId, AllocateBedRequest request) {
        OpdVisit visit = getIpVisit(visitId);
        if (visit.getIpdStatus() == IpdStatus.DISCHARGED || visit.getIpdStatus() == IpdStatus.SETTLED) {
            throw new RuntimeException("Cannot allocate bed for DISCHARGED or SETTLED visit");
        }

        User currentUser = getCurrentUser();

        // Check if bed is already occupied by another active visit
        Optional<BedAllocation> activeBedAllocation = bedAllocationRepository
                .findTopByBedNumberAndReleasedAtIsNull(request.getBedNumber());
        if (activeBedAllocation.isPresent() && !activeBedAllocation.get().getVisit().getId().equals(visit.getId())) {
            throw new RuntimeException("Bed " + request.getBedNumber() + " is already occupied");
        }

        // Release current bed allocation for this visit, if any
        Optional<BedAllocation> currentAllocation = bedAllocationRepository
                .findTopByVisitIdAndReleasedAtIsNull(visit.getId());
        if (currentAllocation.isPresent()) {
            BedAllocation current = currentAllocation.get();
            current.setReleasedAt(Instant.now());
            bedAllocationRepository.save(current);
        }

        // Create new allocation
        BedAllocation newAllocation = BedAllocation.builder()
                .visit(visit)
                .bedNumber(request.getBedNumber())
                .wardName(request.getWardName())
                .allocatedAt(Instant.now())
                .allocatedByUserId(currentUser.getId())
                .build();

        newAllocation = bedAllocationRepository.save(newAllocation);

        eventPublisher.publishEvent(AuditEvent.builder()
                .entityName("BED_ALLOCATION")
                .entityId(newAllocation.getId().toString())
                .actionType("CREATE")
                .newValue("Bed: " + request.getBedNumber() + ", Ward: " + request.getWardName())
                .performedByUserId(currentUser.getUsername())
                .reason("Allocated bed to IPD visit " + visitId)
                .build());

        return mapToBedAllocationDto(newAllocation);
    }

    public List<BedAllocationDto> getBedAllocations(String visitId) {
        OpdVisit visit = getIpVisit(visitId);
        return bedAllocationRepository.findByVisitIdOrderByAllocatedAtDesc(visit.getId())
                .stream().map(this::mapToBedAllocationDto).collect(Collectors.toList());
    }

    @Transactional
    public IpdDailyChargeDto addDailyCharge(String visitId, AddDailyChargeRequest request) {
        OpdVisit visit = getIpVisit(visitId);
        if (visit.isFinalSettlementLocked() || visit.getIpdStatus() == IpdStatus.SETTLED) {
            throw new RuntimeException("Cannot add charges to a locked/settled IPD visit");
        }

        // Idempotency check: Same Visit, Date, and Charge Type
        Optional<IpdDailyCharge> existingCharge = ipdDailyChargeRepository.findByVisitIdAndChargeDateAndChargeType(
                visit.getId(), request.getChargeDate(), request.getChargeType());

        if (existingCharge.isPresent()) {
            throw new RuntimeException("Charge for this date and type already exists");
        }

        User currentUser = getCurrentUser();

        IpdDailyCharge charge = IpdDailyCharge.builder()
                .visit(visit)
                .chargeDate(request.getChargeDate())
                .chargeType(request.getChargeType())
                .amount(request.getAmount())
                .createdByUserId(currentUser.getId())
                .build();

        charge = ipdDailyChargeRepository.save(charge);

        // Add to related Bill directly using a generic dynamic HospitalService
        Bill bill = visit.getBill();
        if (bill == null) {
            throw new RuntimeException("No bill attached to this IPD visit");
        }

        String serviceName = "IPD " + request.getChargeType().name() + " Charge (" + request.getChargeDate().toString()
                + ")";
        HospitalService dummyService = getOrCreateDummyService(request.getChargeType());

        BillItem item = BillItem.builder()
                .bill(bill)
                .service(dummyService)
                .quantity(1)
                .unitPrice(request.getAmount())
                .totalAmount(request.getAmount())
                .build();

        billItemRepository.save(item);

        // Recalculate bill seamlessly
        opdVisitService.recalculateBill(bill);

        eventPublisher.publishEvent(AuditEvent.builder()
                .entityName("IPD_CHARGE")
                .entityId(charge.getId().toString())
                .actionType("CREATE")
                .newValue("Type: " + request.getChargeType() + ", Date: " + request.getChargeDate() + ", Amount: "
                        + request.getAmount())
                .performedByUserId(currentUser.getUsername())
                .reason("Added daily IPD charge to visit " + visitId)
                .build());

        return mapToIpdDailyChargeDto(charge);
    }

    public List<IpdDailyChargeDto> getDailyCharges(String visitId) {
        OpdVisit visit = getIpVisit(visitId);
        return ipdDailyChargeRepository.findByVisitIdOrderByChargeDateDesc(visit.getId())
                .stream().map(this::mapToIpdDailyChargeDto).collect(Collectors.toList());
    }

    @Transactional
    public void changeIpdStatus(String visitId, ChangeIpdStatusRequest request) {
        OpdVisit visit = getIpVisit(visitId);

        IpdStatus oldStatus = visit.getIpdStatus();
        IpdStatus newStatus = request.getStatus();

        if (oldStatus == newStatus)
            return;

        User currentUser = getCurrentUser();
        boolean isDoctor = currentUser.getRole() == Role.DOCTOR;
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isSupervisor = currentUser.getRole() == Role.SUPERVISOR;
        boolean isReception = currentUser.getRole() == Role.RECEPTION; // Usually Billing

        if (newStatus == IpdStatus.READY_FOR_DISCHARGE && !isDoctor) {
            throw new RuntimeException("Only DOCTOR can mark a patient as READY_FOR_DISCHARGE");
        }

        if (newStatus == IpdStatus.DISCHARGED && !(isAdmin || isSupervisor)) {
            throw new RuntimeException("Only ADMIN or SUPERVISOR can mark a patient as DISCHARGED");
        }

        if (newStatus == IpdStatus.SETTLED && !(isAdmin || isReception || isSupervisor)) {
            throw new RuntimeException("Only authorized billing roles can mark as SETTLED");
        }

        if (newStatus == IpdStatus.DISCHARGED) {
            // Validate that we have proper info. In real life, check no pending tasks.
            visit.setDischargeTimestamp(java.time.LocalDateTime.now());
            if (request.getDischargeSummaryNote() != null) {
                visit.setDischargeSummaryNote(request.getDischargeSummaryNote());
            }
        }

        if (newStatus == IpdStatus.SETTLED) {
            Bill bill = visit.getBill();
            if (bill != null && bill.getPaymentStatus() != PaymentStatus.PAID) {
                throw new RuntimeException("Cannot mark as SETTLED unless the bill is fully PAID");
            }
            visit.setFinalSettlementLocked(true);

            // finalize bill locks it
            if (bill != null && bill.getStatus() != BillStatus.LOCKED) {
                opdVisitService.finalizeBill(bill.getBillId());
            }
        }

        visit.setIpdStatus(newStatus);
        opdVisitRepository.save(visit);

        eventPublisher.publishEvent(AuditEvent.builder()
                .entityName("IPD_VISIT")
                .entityId(visitId)
                .actionType("UPDATE_STATUS")
                .oldValue(oldStatus != null ? oldStatus.name() : "NULL")
                .newValue(newStatus.name())
                .performedByUserId(currentUser.getUsername())
                .reason("IPD Workflow transition")
                .build());
    }

    @Transactional
    public void reopenVisit(String visitId, ReopenIpdVisitRequest request) {
        OpdVisit visit = getIpVisit(visitId);

        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only ADMIN can reopen a SETTLED IPD visit");
        }

        if (visit.getIpdStatus() != IpdStatus.SETTLED) {
            throw new RuntimeException("Only SETTLED visits can be reopened");
        }

        visit.setIpdStatus(IpdStatus.UNDER_TREATMENT);
        visit.setFinalSettlementLocked(false);
        visit.setDischargeTimestamp(null);

        // Reopen bill
        Bill bill = visit.getBill();
        if (bill != null) {
            bill.setStatus(BillStatus.PAYMENT_PENDING); // or DRAFT
            // SecurityHash must be regenerated natively via saving
        }

        opdVisitRepository.save(visit);

        eventPublisher.publishEvent(AuditEvent.builder()
                .entityName("IPD_VISIT")
                .entityId(visitId)
                .actionType("REOPEN")
                .oldValue("SETTLED")
                .newValue("UNDER_TREATMENT")
                .performedByUserId(currentUser.getUsername())
                .reason(request.getReason())
                .build());
    }

    // --- Helpers ---

    private HospitalService getOrCreateDummyService(IpdChargeType chargeType) {
        String serviceId = "IPD_" + chargeType.name();
        return hospitalServiceRepository.findByServiceId(serviceId).orElseGet(() -> {
            HospitalService svc = new HospitalService();
            svc.setServiceId(serviceId);
            svc.setServiceName("IPD " + chargeType.name() + " Charge");
            svc.setCategory(
                    chargeType == IpdChargeType.ROOM ? ServiceCategory.ROOM_BED : ServiceCategory.MISCELLANEOUS);
            svc.setDefaultPrice(BigDecimal.ZERO);
            svc.setUnit("Per Day/Unit");
            svc.setActive(true);
            svc.setCreatedBy("SYSTEM");
            return hospitalServiceRepository.save(svc);
        });
    }

    private BedAllocationDto mapToBedAllocationDto(BedAllocation allocation) {
        return BedAllocationDto.builder()
                .id(allocation.getId())
                .visitId(allocation.getVisit().getVisitId())
                .bedNumber(allocation.getBedNumber())
                .wardName(allocation.getWardName())
                .allocatedAt(allocation.getAllocatedAt())
                .releasedAt(allocation.getReleasedAt())
                .allocatedByUserId(allocation.getAllocatedByUserId())
                .build();
    }

    private IpdDailyChargeDto mapToIpdDailyChargeDto(IpdDailyCharge charge) {
        return IpdDailyChargeDto.builder()
                .id(charge.getId())
                .visitId(charge.getVisit().getVisitId())
                .chargeDate(charge.getChargeDate())
                .chargeType(charge.getChargeType())
                .amount(charge.getAmount())
                .createdByUserId(charge.getCreatedByUserId())
                .createdAt(charge.getCreatedAt())
                .build();
    }
}
