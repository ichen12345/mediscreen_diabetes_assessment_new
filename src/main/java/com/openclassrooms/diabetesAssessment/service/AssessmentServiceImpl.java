package com.openclassrooms.diabetesAssessment.service;

import com.openclassrooms.diabetesAssessment.entity.Patient;
import com.openclassrooms.diabetesAssessment.entity.PatientNotesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
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
        try {
            // Fetch patient details by name
            Patient patient = restTemplate.getForObject(
                    DEMOGRAPHIC_SERVICE_URL + "/api/patients/search?family=" + family + "&given=" + given, Patient.class);

            if (patient == null) {
                throw new EntityNotFoundException("Patient not found with name: " + family + " " + given);
            }

            // Proceed with risk assessment
            return assessRiskForPatient(patient);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new EntityNotFoundException("Patient not found with name: " + family + " " + given);
            } else {
                throw e; // rethrow other HTTP exceptions if necessary
            }
        }
    }


    public String assessRiskById(Long patientId) {
        // Fetch patient details by ID
        Patient patient = restTemplate.getForObject(
                DEMOGRAPHIC_SERVICE_URL + "/api/patients/" + patientId, Patient.class);

        // Throw an exception if the patient is not found
        if (patient == null) {
            throw new EntityNotFoundException("Patient not found with ID: " + patientId);
        }

        return assessRiskForPatient(patient);
    }

    private String assessRiskForPatient(Patient patient) {
        // Fetch doctor notes
        String patId = String.valueOf(patient.getPatientId());
        PatientNotesResponse response = restTemplate.getForObject(
                NOTES_SERVICE_URL + "/api/patHistory/" + patId, PatientNotesResponse.class);

        List<String> doctorNotes = (response != null) ? response.getNote() : Collections.emptyList();
        System.out.println("Doctor notes: " + doctorNotes);

        // Calculate trigger count, age, and determine risk
        int triggerCount = countTriggers(doctorNotes);
        System.out.println("Final trigger count: " + triggerCount); // Debug statement

        if (patient.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required for assessment");
        }

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
            if ("M".equalsIgnoreCase(sex)) {
                if (triggerCount >= 5) {
                    return "Early Onset"; // Under 30 male with 5 or more triggers
                } else if (triggerCount >= 3) {
                    return "In danger"; // Under 30 male with 3 triggers
                }
            } else if ("F".equalsIgnoreCase(sex)) {
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
            System.out.println("Checking note: " + note); // Debug output for each note
            for (String trigger : TRIGGERS) {
                // Check if the note contains the trigger (case-insensitive)
                if (note.toLowerCase().contains(trigger.toLowerCase())) {
                    count++;
                    System.out.println("Trigger found: " + trigger); // Debug output for found trigger
                    // No break statement here, continue checking for more triggers in the same note
                }
            }
        }
        System.out.println("Total trigger count: " + count); // Final count output
        return count;
    }

    protected int calculateAge(LocalDate dob) {
        return java.time.Period.between(dob, java.time.LocalDate.now()).getYears();
    }

    public List<Patient> getAllPatients() {
        return restTemplate.exchange(
                DEMOGRAPHIC_SERVICE_URL + "/api/patients",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Patient>>() {}).getBody();
    }

}
