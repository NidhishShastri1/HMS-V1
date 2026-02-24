package com.hms.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BillItemDto {
    private Long id;
    private String serviceId;
    private String serviceName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}
