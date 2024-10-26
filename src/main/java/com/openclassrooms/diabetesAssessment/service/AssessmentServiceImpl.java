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


    public String assessRiskByName(String family, String given) {
        // Fetch patient details by family
        String url = DEMOGRAPHIC_SERVICE_URL + "/api/patients/search?family=" + family + "&given=" + given;
        System.out.println("Fetching patient from: " + url); // Log the URL for debugging
        Patient patient = restTemplate.getForObject(url, Patient.class);
        if (patient == null) {
            return "Patient not found";
        }
        return assessRiskForPatient(patient);
    }



    public String assessRiskById(Long patientId) {
        // Fetch patient details by ID
        Patient patient = restTemplate.getForObject(
                DEMOGRAPHIC_SERVICE_URL + "/api/patients/" + patientId, Patient.class);
        if (patient == null) {
            return "Patient not found";
        }

        return assessRiskForPatient(patient);
    }

    private String assessRiskForPatient(Patient patient) {
        // Fetch doctor notes
        String patId = String.valueOf(patient.getPatientId());
        PatientNotesResponse response = restTemplate.getForObject(
                NOTES_SERVICE_URL + "/patHistory/" + patId, PatientNotesResponse.class);
        List<String> doctorNotes = response.getNote();
        System.out.println("Doctor notes: " + doctorNotes);

        // Calculate trigger count, age, and determine risk
        int triggerCount = countTriggers(doctorNotes);
        int age = calculateAge(patient.getDateOfBirth());
        patient.setAge(age);
        System.out.println("Calculated age: " + age);

        String riskLevel = determineRiskLevel(triggerCount, age, patient.getSex());
        System.out.println("Risk level determined: " + riskLevel);

        return String.format("Patient: %s %s (age %d) diabetes assessment is: %s",
                patient.getGivenName(), patient.getFamily(), age, riskLevel);
    }

    protected String determineRiskLevel(int triggerCount, int age, String sex) {
        if (triggerCount == 0) {
            return "None";
        } else if (age > 30) {
            if (triggerCount >= 8) {
                return "Early Onset"; // Over 30 and 8 or more triggers
            } else if (triggerCount >= 6) {
                return "In danger"; // Over 30 and 6 triggers
            } else if (triggerCount >= 2) {
                return "Borderline"; // Over 30 and 2 or more triggers
            }
        } else { // Age <= 30
            if (sex.equalsIgnoreCase("M")) {
                if (triggerCount >= 5) {
                    return "Early Onset"; // Under 30 male with 5 or more triggers
                } else if (triggerCount >= 3) {
                    return "In danger"; // Under 30 male with 3 triggers
                }
            } else if (sex.equalsIgnoreCase("F")) {
                if (triggerCount >= 7) {
                    return "Early Onset"; // Under 30 female with 7 or more triggers
                } else if (triggerCount >= 4) {
                    return "In danger"; // Under 30 female with 4 triggers
                }
            }
        }
        return "None"; // Fallback, though the case should be covered above
    }


    protected int countTriggers(List<String> doctorNotes) {
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

    protected int calculateAge(LocalDate dob) {
        return java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
    }
}
