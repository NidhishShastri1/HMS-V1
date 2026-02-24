package com.hms.backend.dto;

import com.hms.backend.model.Patient;
import java.time.format.DateTimeFormatter;

public class PatientDto {
    private Long id;
    private String patientId;
    private String firstName;
    private String lastName;
    private String phone;
    private String gender;
    private String dateOfBirth;
    private Integer age;
    private String address;
    private String idProof;
    private String registrationDate;
    private String createdBy;
    private String registrationType;

    public PatientDto(Patient patient) {
        this.id = patient.getId();
        this.patientId = patient.getPatientId();
        this.firstName = patient.getFirstName();
        this.lastName = patient.getLastName();
        this.phone = patient.getPhone();
        this.gender = patient.getGender();
        this.dateOfBirth = patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : null;
        this.age = patient.getAge();
        this.address = patient.getAddress();
        this.idProof = patient.getIdProof();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.registrationDate = patient.getRegistrationDate() != null ? patient.getRegistrationDate().format(formatter)
                : null;
        this.createdBy = patient.getCreatedBy();
        this.registrationType = patient.getRegistrationType() != null ? patient.getRegistrationType().name() : null;
    }

    public Long getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public String getIdProof() {
        return idProof;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getRegistrationType() {
        return registrationType;
    }
}
