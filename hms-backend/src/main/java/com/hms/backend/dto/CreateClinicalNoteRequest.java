package com.hms.backend.dto;

import com.hms.backend.model.ClinicalNoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateClinicalNoteRequest {
    @NotNull(message = "Note type is required")
    private ClinicalNoteType noteType;

    @NotBlank(message = "Note content cannot be blank")
    private String noteContent;
}
