package com.hms.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddServiceToBillRequest {

    @NotBlank(message = "Service ID is mandatory")
    private String serviceId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;

    private java.math.BigDecimal overriddenPrice;
}
