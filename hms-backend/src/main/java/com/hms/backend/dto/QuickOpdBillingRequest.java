package com.hms.backend.dto;

import com.hms.backend.model.VisitCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuickOpdBillingRequest {

    // Patient Details
    @NotBlank(message = "First Name is mandatory")
    private String firstName;

    @NotBlank(message = "Last Name is mandatory")
    private String lastName;

    @NotBlank(message = "Phone number is mandatory")
    private String phone;

    @NotNull(message = "Age is mandatory")
    private Integer age;

    @NotBlank(message = "Gender is mandatory")
    private String gender;

    private String address;

    // Visit Details
    @NotBlank(message = "Assigned Doctor is mandatory")
    private String assignedDoctor;

    @NotBlank(message = "Department is mandatory")
    private String department;

    @NotNull(message = "Visit category is mandatory")
    private VisitCategory visitCategory;

    private String notes;
}
