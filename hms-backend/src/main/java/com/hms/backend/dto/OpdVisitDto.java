package com.hms.backend.dto;

import com.hms.backend.model.VisitCategory;
import com.hms.backend.model.VisitStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OpdVisitDto {
    private String visitId;
    private String patientId;
    private String patientName;
    private String assignedDoctor;
    private String department;
    private VisitCategory visitCategory;
    private LocalDateTime visitDateTime;
    private VisitStatus status;
    private String notes;
    private String billId;
    private LocalDateTime createdAt;
    private String createdBy;
}
