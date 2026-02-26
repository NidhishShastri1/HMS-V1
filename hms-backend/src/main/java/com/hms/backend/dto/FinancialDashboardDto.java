package com.hms.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class FinancialDashboardDto {
    private BigDecimal todaysTotal;
    private Map<String, BigDecimal> paymentModeBreakdown; // Cash vs Digital
    private long totalAdjustments;
    private long totalOverrides;
    private long integrityViolations;
}
