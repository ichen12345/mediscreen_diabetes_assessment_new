package com.openclassrooms.diabetesAssessment.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patient {
    @JsonProperty("id")  // Mapping "id" from JSON to "patientId"
    private Long patientId;

    @JsonProperty("given")
    private String givenName;

    @JsonProperty("family")
    private String family;

    @JsonProperty("dob")  // Mapping "dob" from JSON to "dateOfBirth"
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @JsonProperty("sex")
    private String sex;

    private int age;

    private String riskLevel;

}
