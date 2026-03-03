package com.hms.backend.controller;

import com.hms.backend.dto.*;
import com.hms.backend.service.IpdService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ipd")
public class IpdController {

    @Autowired
    private IpdService ipdService;

    @PostMapping("/{visitId}/allocate-bed")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'SUPERVISOR')")
    public ResponseEntity<BedAllocationDto> allocateBed(@PathVariable String visitId,
            @Valid @RequestBody AllocateBedRequest request) {
        return ResponseEntity.ok(ipdService.allocateBed(visitId, request));
    }

    @GetMapping("/{visitId}/allocate-bed")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'SUPERVISOR', 'DOCTOR')")
    public ResponseEntity<List<BedAllocationDto>> getBedAllocations(@PathVariable String visitId) {
        return ResponseEntity.ok(ipdService.getBedAllocations(visitId));
    }

    @PostMapping("/{visitId}/add-daily-charge")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'SUPERVISOR')")
    public ResponseEntity<IpdDailyChargeDto> addDailyCharge(@PathVariable String visitId,
            @Valid @RequestBody AddDailyChargeRequest request) {
        return ResponseEntity.ok(ipdService.addDailyCharge(visitId, request));
    }

    @GetMapping("/{visitId}/add-daily-charge")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'SUPERVISOR', 'DOCTOR')")
    public ResponseEntity<List<IpdDailyChargeDto>> getDailyCharges(@PathVariable String visitId) {
        return ResponseEntity.ok(ipdService.getDailyCharges(visitId));
    }

    @PostMapping("/{visitId}/change-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTION', 'SUPERVISOR', 'DOCTOR')")
    public ResponseEntity<Void> changeStatus(@PathVariable String visitId,
            @Valid @RequestBody ChangeIpdStatusRequest request) {
        ipdService.changeIpdStatus(visitId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{visitId}/reopen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reopenVisit(@PathVariable String visitId,
            @Valid @RequestBody ReopenIpdVisitRequest request) {
        ipdService.reopenVisit(visitId, request);
        return ResponseEntity.ok().build();
    }
}
