package com.hms.backend.dto;

import com.hms.backend.model.ServicePriceHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryDto {
    private String serviceId;
    private String serviceName;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String changedBy;
    private String reason;
    private LocalDateTime changedAt;

    public PriceHistoryDto(ServicePriceHistory history) {
        this.serviceId = history.getService().getServiceId();
        this.serviceName = history.getService().getServiceName();
        this.oldPrice = history.getOldPrice();
        this.newPrice = history.getNewPrice();
        this.changedBy = history.getChangedBy();
        this.reason = history.getReason();
        this.changedAt = history.getChangedAt();
    }
}
