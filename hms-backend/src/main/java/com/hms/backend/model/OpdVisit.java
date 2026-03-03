package com.hms.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "opd_visits")
public class OpdVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String visitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private String assignedDoctor;

    @Column(nullable = false)
    private LocalDateTime visitDateTime;

    @Column(nullable = false)
    private String department;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitCategory visitCategory = VisitCategory.CASH;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitStatus status = VisitStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "visit_status", nullable = false, columnDefinition = "varchar(255) default 'CREATED'")
    @Builder.Default
    private ClinicalVisitStatus visitStatus = ClinicalVisitStatus.CREATED;

    @Column(name = "primary_doctor_id")
    private Long primaryDoctorId;

    @Column(name = "clinical_locked", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean clinicalLocked = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "ipd_status")
    private IpdStatus ipdStatus;

    @Column(name = "admission_timestamp")
    private LocalDateTime admissionTimestamp;

    @Column(name = "discharge_timestamp")
    private LocalDateTime dischargeTimestamp;

    @Column(name = "discharge_summary_note", columnDefinition = "TEXT")
    private String dischargeSummaryNote;

    @Column(name = "final_settlement_locked", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean finalSettlementLocked = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "visit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Bill bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false, updatable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
