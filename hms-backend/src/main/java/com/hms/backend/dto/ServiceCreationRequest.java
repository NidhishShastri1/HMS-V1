package com.hms.backend.dto;

import com.hms.backend.model.ServiceCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ServiceCreationRequest {
    @NotBlank(message = "Service Name is required")
    private String serviceName;

    @NotNull(message = "Category is required")
    private ServiceCategory category;

    @NotNull(message = "Default Price is required")
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private BigDecimal defaultPrice;

    @NotBlank(message = "Unit is required")
    private String unit;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public void setDefaultPrice(BigDecimal defaultPrice) {
        this.defaultPrice = defaultPrice;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
