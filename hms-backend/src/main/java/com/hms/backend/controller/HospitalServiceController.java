package com.hms.backend.controller;

import com.hms.backend.dto.PriceChangeRequest;
import com.hms.backend.dto.PriceHistoryDto;
import com.hms.backend.dto.ServiceCreationRequest;
import com.hms.backend.dto.ServiceDto;
import com.hms.backend.service.HospitalServiceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class HospitalServiceController {

    private final HospitalServiceService serviceService;

    public HospitalServiceController(HospitalServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllServices(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(serviceService.getAllServices(activeOnly));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDto> createService(@Valid @RequestBody ServiceCreationRequest request,
            Authentication auth) {
        ServiceDto created = serviceService.createService(request, auth.getName());
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{serviceId}/price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDto> updateServicePrice(
            @PathVariable String serviceId,
            @Valid @RequestBody PriceChangeRequest request,
            Authentication auth) {
        ServiceDto updated = serviceService.updatePrice(serviceId, request, auth.getName());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{serviceId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDto> toggleServiceStatus(@PathVariable String serviceId, Authentication auth) {
        return ResponseEntity.ok(serviceService.toggleActiveStatus(serviceId, auth.getName()));
    }

    @GetMapping("/{serviceId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PriceHistoryDto>> getServiceHistory(@PathVariable String serviceId) {
        return ResponseEntity.ok(serviceService.getPriceHistory(serviceId));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PriceHistoryDto>> getAllGlobalPriceHistory() {
        return ResponseEntity.ok(serviceService.getAllGlobalPriceHistory());
    }
}
