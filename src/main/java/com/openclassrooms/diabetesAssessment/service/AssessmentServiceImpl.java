package com.openclassrooms.diabetesAssessment.service;

import com.openclassrooms.diabetesAssessment.entity.Patient;
import com.openclassrooms.diabetesAssessment.entity.PatientNotesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssessmentServiceImpl implements AssessmentService {

    @Autowired
    private RestTemplate restTemplate;

    private final String DEMOGRAPHIC_SERVICE_URL = "http://localhost:8081";
    private final String NOTES_SERVICE_URL = "http://localhost:8082";

    private static final List<String> TRIGGERS = List.of(
            "Hemoglobin A1C", "Microalbumin", "Body Height",
            "Body Weight", "Smoker", "Abnormal",
            "Cholesterol", "Dizziness", "Relapse",
            "Reaction", "Antibodies"
    );

    @Override
    public String assessRisk(Long patientId) {

        // Fetch patient details
        Patient patient = restTemplate.getForObject(DEMOGRAPHIC_SERVICE_URL + "/api/patients/" + patientId, Patient.class);
        if (patient == null) {
            return "Patient not found";
        }
        System.out.println("Date of Birth: " + patient.getDateOfBirth());
        // Fetch doctor notes using the correct DTO
        String patId = String.valueOf(patientId);
        PatientNotesResponse response = restTemplate.getForObject(NOTES_SERVICE_URL + "/patHistory/" + patId, PatientNotesResponse.class);
        List<String> doctorNotes = response.getNote(); // Extract the notes
        System.out.println("Doctor notes: " + doctorNotes);

        // Calculate the number of trigger words in doctor notes
        int triggerCount = countTriggers(doctorNotes);
        int age = calculateAge(patient.getDateOfBirth());
        patient.setAge(age);
        System.out.println("Calculated age: " + age);

        // Determine risk level based on triggers, age, and sex
        String riskLevel = determineRiskLevel(triggerCount, age, patient.getSex());
        System.out.println("Risk level determined: " + riskLevel);

        return String.format("Patient: %s %s (age %d) diabetes assessment is: %s",
                patient.getGivenName(), patient.getFamilyName(), age, riskLevel);
    }


    private String determineRiskLevel(int triggerCount, int age, String sex) {
        if (triggerCount == 0) {
            return "None";
        } else if (triggerCount >= 2 && age > 30) {
            return "Borderline";
        } else if (age < 30) {
            if (sex.equalsIgnoreCase("M") && triggerCount >= 3) {
                return "In danger";
            } else if (sex.equalsIgnoreCase("F") && triggerCount >= 4) {
                return "In danger";
            } else if (sex.equalsIgnoreCase("M") && triggerCount >= 5) {
                return "Early Onset";
            } else if (sex.equalsIgnoreCase("F") && triggerCount >= 7) {
                return "Early Onset";
            }
        } else if (age > 30) {
            if (triggerCount >= 6) {
                return "In danger";
            } else if (triggerCount >= 8) {
                return "Early Onset";
            }
        }
        return "None"; // Default case
    }

    private int countTriggers(List<String> doctorNotes) {
        int count = 0;
        for (String note : doctorNotes) {
            for (String trigger : TRIGGERS) {
                if (note.contains(trigger)) {
                    count++;
                    break; // Stop checking this note after finding one trigger
                }
            }
        }
        return count;
    }

    private int calculateAge(LocalDate dob) {
        return java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
    }
}
