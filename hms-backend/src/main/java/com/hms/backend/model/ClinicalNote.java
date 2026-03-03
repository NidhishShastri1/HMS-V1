package com.hms.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "clinical_note")
public class ClinicalNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private OpdVisit visit;

    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false)
    private ClinicalNoteType noteType;

    @Column(name = "note_content", columnDefinition = "LONGTEXT")
    private String noteContent;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent;
}
