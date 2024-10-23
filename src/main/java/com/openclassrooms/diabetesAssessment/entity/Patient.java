package com.openclassrooms.diabetesAssessment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    private Long patientId;
    private String givenName;
    private String familyName;
    private LocalDate dateOfBirth;
    private String sex;
    private int age;
    private String riskLevel;

}
