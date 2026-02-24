package com.hms.backend.dto;

import com.hms.backend.model.BillStatus;
import com.hms.backend.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BillDto {
    private String billId;
    private String visitId;
    private String patientId;
    private String patientName;
    private Integer age;
    private String gender;
    private String phone;
    private BigDecimal subTotal;
    private BigDecimal tax;
    private BigDecimal discount;
    private BigDecimal grandTotal;
    private BillStatus status;
    private String amountInWords;
    private PaymentStatus paymentStatus;
    private List<BillItemDto> items;
    private List<PaymentDto> payments;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
