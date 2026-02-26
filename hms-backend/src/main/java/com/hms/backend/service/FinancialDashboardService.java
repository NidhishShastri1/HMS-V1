package com.hms.backend.service;

import com.hms.backend.dto.FinancialDashboardDto;
import com.hms.backend.model.PaymentMode;
import com.hms.backend.repository.BillAdjustmentRepository;
import com.hms.backend.repository.BillRepository;
import com.hms.backend.repository.PaymentRepository;
import com.hms.backend.repository.SecurityEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialDashboardService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private BillAdjustmentRepository billAdjustmentRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Transactional(readOnly = true)
    public FinancialDashboardDto getDashboardStats() {
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);

        BigDecimal todaysTotal = paymentRepository.sumAmountByPaymentDateTimeAfter(startOfToday);
        if (todaysTotal == null)
            todaysTotal = BigDecimal.ZERO;

        List<Object[]> modeData = paymentRepository.sumAmountGroupByPaymentModeAfter(startOfToday);
        Map<String, BigDecimal> breakdown = new HashMap<>();
        for (Object[] row : modeData) {
            PaymentMode mode = (PaymentMode) row[0];
            BigDecimal sum = (BigDecimal) row[1];
            breakdown.put(mode.name(), sum);
        }

        long adjustedBills = billRepository.countByIsAdjustedTrue();
        long manualOverrides = billAdjustmentRepository.countByAdjustmentType("MANUAL_OVERRIDE");
        long integrityViolations = securityEventRepository.countByEventType("INTEGRITY_VIOLATION");

        return FinancialDashboardDto.builder()
                .todaysTotal(todaysTotal)
                .paymentModeBreakdown(breakdown)
                .totalAdjustments(adjustedBills)
                .totalOverrides(manualOverrides)
                .integrityViolations(integrityViolations)
                .build();
    }
}
