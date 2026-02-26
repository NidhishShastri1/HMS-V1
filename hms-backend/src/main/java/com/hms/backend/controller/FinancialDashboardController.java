package com.hms.backend.controller;

import com.hms.backend.dto.FinancialDashboardDto;
import com.hms.backend.service.FinancialDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financial-dashboard")
public class FinancialDashboardController {

    @Autowired
    private FinancialDashboardService financialDashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<FinancialDashboardDto> getStats() {
        return ResponseEntity.ok(financialDashboardService.getDashboardStats());
    }
}
