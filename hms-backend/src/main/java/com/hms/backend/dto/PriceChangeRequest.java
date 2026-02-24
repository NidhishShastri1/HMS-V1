package com.hms.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PriceChangeRequest {
    @NotNull(message = "New Price is required")
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private BigDecimal newPrice;

    @NotBlank(message = "Reason for price change is required")
    private String reason;

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(BigDecimal newPrice) {
        this.newPrice = newPrice;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
