package com.hms.backend.controller;

import com.hms.backend.dto.*;
import com.hms.backend.service.OpdVisitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opd")
public class OpdVisitController {

    @Autowired
    private OpdVisitService opdVisitService;

    @PostMapping("/visits")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<OpdVisitDto> createVisit(@Valid @RequestBody CreateOpdVisitRequest request) {
        return ResponseEntity.ok(opdVisitService.createOpdVisit(request));
    }

    @PostMapping("/quick-billing")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<BillDto> createQuickBilling(@Valid @RequestBody QuickOpdBillingRequest request) {
        return ResponseEntity.ok(opdVisitService.createQuickOpdBilling(request));
    }

    @GetMapping("/visits")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'SUPERVISOR')")
    public ResponseEntity<List<OpdVisitDto>> getAllVisits() {
        return ResponseEntity.ok(opdVisitService.getAllVisits());
    }

    @GetMapping("/visits/{visitId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'SUPERVISOR')")
    public ResponseEntity<OpdVisitDto> getVisit(@PathVariable String visitId) {
        return ResponseEntity.ok(opdVisitService.getVisitById(visitId));
    }

    @GetMapping("/bills/{billId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'SUPERVISOR')")
    public ResponseEntity<BillDto> getBill(@PathVariable String billId) {
        return ResponseEntity.ok(opdVisitService.getBillById(billId));
    }

    @PostMapping("/bills/{billId}/services")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<BillDto> addService(@PathVariable String billId,
            @Valid @RequestBody AddServiceToBillRequest request) {
        return ResponseEntity.ok(opdVisitService.addServiceToBill(billId, request));
    }

    @DeleteMapping("/bills/{billId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<BillDto> removeService(@PathVariable String billId, @PathVariable Long itemId) {
        return ResponseEntity.ok(opdVisitService.removeServiceFromBill(billId, itemId));
    }

    @PostMapping("/bills/{billId}/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<BillDto> addPayment(@PathVariable String billId,
            @Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(opdVisitService.addPayment(billId, request));
    }

    @PostMapping("/bills/{billId}/finalize")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION')")
    public ResponseEntity<BillDto> finalizeBill(@PathVariable String billId) {
        return ResponseEntity.ok(opdVisitService.finalizeBill(billId));
    }

    @PostMapping("/bills/{billId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BillDto> cancelBill(@PathVariable String billId) {
        return ResponseEntity.ok(opdVisitService.cancelBill(billId));
    }
}
