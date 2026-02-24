package com.hms.backend.dto;

import com.hms.backend.model.ServicePriceHistory;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class PriceHistoryDto {
    private String serviceId;
    private String serviceName;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String changedBy;
    private String reason;
    private String changedAt;

    public PriceHistoryDto(ServicePriceHistory history) {
        this.serviceId = history.getService().getServiceId();
        this.serviceName = history.getService().getServiceName();
        this.oldPrice = history.getOldPrice();
        this.newPrice = history.getNewPrice();
        this.changedBy = history.getChangedBy();
        this.reason = history.getReason();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.changedAt = history.getChangedAt() != null ? history.getChangedAt().format(formatter) : null;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public String getReason() {
        return reason;
    }

    public String getChangedAt() {
        return changedAt;
    }
}
