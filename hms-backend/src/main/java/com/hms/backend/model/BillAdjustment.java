package com.hms.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bill_adjustments", indexes = {
        @Index(name = "idx_adj_type", columnList = "adjustment_type")
})
public class BillAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adjusted_by_user_id", nullable = false)
    private User adjustedBy;

    @CreationTimestamp
    @Column(name = "adjusted_at", nullable = false, updatable = false)
    private LocalDateTime adjustedAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustedAmount;

    @Column(nullable = false)
    private String reason;

    @Column
    private String adjustmentType; // e.g. "OVERRIDE", "DISCOUNT"
}
