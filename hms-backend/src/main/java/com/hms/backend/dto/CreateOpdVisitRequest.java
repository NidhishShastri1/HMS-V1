package com.hms.backend.dto;

import com.hms.backend.model.VisitCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateOpdVisitRequest {

    @NotBlank(message = "Patient ID is mandatory")
    private String patientId;

    @NotBlank(message = "Assigned Doctor is mandatory")
    private String assignedDoctor;

    @NotNull(message = "Visit date and time is mandatory")
    private LocalDateTime visitDateTime;

    @NotBlank(message = "Department is mandatory")
    private String department;

    @NotNull(message = "Visit category is mandatory")
    private VisitCategory visitCategory;

    private String notes;
}
