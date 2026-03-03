package com.hms.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bed_allocation")
public class BedAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private OpdVisit visit;

    @Column(name = "bed_number", nullable = false)
    private String bedNumber;

    @Column(name = "ward_name", nullable = false)
    private String wardName;

    @Column(name = "allocated_at", nullable = false)
    private Instant allocatedAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "allocated_by_user_id", nullable = false)
    private Long allocatedByUserId;
}
