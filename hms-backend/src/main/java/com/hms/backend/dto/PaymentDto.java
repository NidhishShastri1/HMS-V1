package com.hms.backend.dto;

import com.hms.backend.model.PaymentMode;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentDto {
    private String receiptId;
    private PaymentMode paymentMode;
    private BigDecimal amount;
    private LocalDateTime paymentDateTime;
    private String receivedBy;
}
