package com.hms.backend.dto;

import com.hms.backend.model.HospitalService;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ServiceDto {
    private String serviceId;
    private String serviceName;
    private String category;
    private BigDecimal defaultPrice;
    private String unit;
    private boolean isActive;
    private String createdBy;
    private String updatedAt;

    public ServiceDto() {
    }

    public ServiceDto(HospitalService service) {
        this.serviceId = service.getServiceId();
        this.serviceName = service.getServiceName();
        this.category = service.getCategory().name();
        this.defaultPrice = service.getDefaultPrice();
        this.unit = service.getUnit();
        this.isActive = service.isActive();
        this.createdBy = service.getCreatedBy();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.updatedAt = service.getUpdatedAt() != null ? service.getUpdatedAt().format(formatter) : null;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
