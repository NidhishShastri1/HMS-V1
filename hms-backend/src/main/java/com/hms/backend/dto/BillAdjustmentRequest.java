package com.hms.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillAdjustmentRequest {

    @NotNull(message = "Adjustment amount is mandatory")
    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    private BigDecimal adjustedAmount;

    @NotBlank(message = "Reason for adjustment is mandatory")
    private String reason;
}
