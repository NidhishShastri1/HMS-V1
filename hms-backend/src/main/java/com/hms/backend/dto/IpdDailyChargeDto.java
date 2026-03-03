package com.hms.backend.dto;

import com.hms.backend.model.IpdChargeType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class IpdDailyChargeDto {
    private Long id;
    private String visitId;
    private LocalDate chargeDate;
    private IpdChargeType chargeType;
    private BigDecimal amount;
    private Long createdByUserId;
    private Instant createdAt;
}
