package com.hms.backend.dto;

import com.hms.backend.model.IpdChargeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddDailyChargeRequest {
    @NotNull(message = "Charge date is required")
    private LocalDate chargeDate;

    @NotNull(message = "Charge type is required")
    private IpdChargeType chargeType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}
