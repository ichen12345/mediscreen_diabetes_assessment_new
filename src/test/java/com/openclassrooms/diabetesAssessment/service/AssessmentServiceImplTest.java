package com.openclassrooms.diabetesAssessment.service;

import com.openclassrooms.diabetesAssessment.entity.Patient;
import com.openclassrooms.diabetesAssessment.entity.PatientNotesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssessmentServiceImplTest {

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    @Mock
    private RestTemplate restTemplate;

    private List<Patient> patients;

    private Patient patient1;
    private PatientNotesResponse notesResponse;

    @BeforeEach
    void setUp() {
        patient1 = new Patient();
        patient1.setPatientId(1L);
        patient1.setFamily("Doe");
        patient1.setGivenName("John");
        patient1.setDateOfBirth(LocalDate.of(1985, 5, 15));
        patient1.setSex("M");

        Patient patient2 = new Patient();
        patient2.setPatientId(2L);
        patient2.setFamily("Smith");
        patient2.setGivenName("Jane");
        patient2.setSex("F");

        patients = Arrays.asList(patient1, patient2);

        notesResponse = new PatientNotesResponse();
        notesResponse.setNote(List.of("Patient has Hemoglobin A1C levels high", "Shows signs of Dizziness"));
    }

    @Test
    void testAssessRiskById_Success() {
        // Arrange
        Long patient1Id = 1L;
        Patient patient1 = new Patient();
        patient1.setPatientId(patient1Id);
        patient1.setFamily("Doe");
        patient1.setGivenName("John");
        patient1.setDateOfBirth(LocalDate.of(1985, 5, 15)); // Set a sample DOB
        patient1.setSex("M"); // Set a sample sex

        // Mock the REST call to return the patient1
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(patient1);

        // Mock the notes response
        PatientNotesResponse notesResponse = new PatientNotesResponse();
        notesResponse.setNote(List.of("Body Weight", "Smoker")); // Example notes that trigger a risk
        when(restTemplate.getForObject(anyString(), eq(PatientNotesResponse.class))).thenReturn(notesResponse);

        // Act
        String result = assessmentService.assessRiskById(patient1Id);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("diabetes assessment is: Borderline")); // Update based on expected output
    }

    @Test
    void testAssessRiskById_PatientNotFound() {
        // Arrange
        Long patient1Id = 2L;
        // Mock the REST call to return null, simulating patient1 not found
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            assessmentService.assessRiskById(patient1Id);
        });

        assertEquals("Patient not found with ID: " + patient1Id, exception.getMessage());
    }

    @Test
    void testAssessRiskByName_Success() {
        // Arrange
        Patient patient1 = new Patient();
        patient1.setPatientId(1L);
        patient1.setFamily("Doe");
        patient1.setGivenName("John");
        patient1.setDateOfBirth(LocalDate.of(1990, 1, 1)); // Setting a valid DOB

        // Mock the patient1 response from the REST call
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(patient1);

        // Mock the notes response from the REST call
        PatientNotesResponse notesResponse = new PatientNotesResponse();
        notesResponse.setNote(List.of("Body Height", "Abnormal")); // Two triggers
        when(restTemplate.getForObject(anyString(), eq(PatientNotesResponse.class))).thenReturn(notesResponse);

        // Call the method under test
        String result = assessmentService.assessRiskByName("Doe", "John");

        // Validate the result
        assertNotNull(result);
        assertTrue(result.contains("diabetes assessment is: Borderline"));
    }

    @Test
    void testAssessRiskByName_PatientNotFound() {
        // Arrange
        String familyName = "Doe";
        String givenName = "John";

        // Mock the patient1 response to be null, simulating patient1 not found
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(null);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            assessmentService.assessRiskByName(familyName, givenName);
        });
    }

    @Test
    void testAssessRiskByName_NoDateOfBirth() {
        // Arrange
        Patient patient1 = new Patient();
        patient1.setPatientId(1L);
        patient1.setFamily("Doe");
        patient1.setGivenName("John");
        patient1.setDateOfBirth(null); // No DOB

        // Mock the patient1 response from the REST call
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(patient1);

        // Mock the notes response from the REST call
        PatientNotesResponse notesResponse = new PatientNotesResponse();
        notesResponse.setNote(List.of("Body Height", "Abnormal")); // Two triggers
        when(restTemplate.getForObject(anyString(), eq(PatientNotesResponse.class))).thenReturn(notesResponse);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            assessmentService.assessRiskByName("Doe", "John");
        });
    }

    @Test
    void testCountTriggers() {
        List<String> doctorNotes = List.of("Body Height", "Smoker", "Cholesterol");
        int count = assessmentService.countTriggers(doctorNotes);
        assertEquals(3, count); // All notes match triggers
    }

    @Test
    void testCountTriggers_NoMatches() {
        List<String> doctorNotes = List.of("Healthy", "Regular Checkup");
        int count = assessmentService.countTriggers(doctorNotes);
        assertEquals(0, count); // No notes match triggers
    }

    @Test
    void testDetermineRiskLevel_None() {
        String riskLevel = assessmentService.determineRiskLevel(0, 25, "M");
        assertEquals("None", riskLevel);
    }

    @Test
    void testDetermineRiskLevel_Borderline() {
        String riskLevel = assessmentService.determineRiskLevel(3, 31, "M");
        assertEquals("Borderline", riskLevel);
    }

    @Test
    void testDetermineRiskLevel_InDanger_YoungFemale() {
        String riskLevel = assessmentService.determineRiskLevel(4, 29, "F");
        assertEquals("In danger", riskLevel);
    }

    @Test
    void testDetermineRiskLevel_EarlyOnset_YoungFemale() {
        String riskLevel = assessmentService.determineRiskLevel(7, 29, "F");
        assertEquals("Early Onset", riskLevel);
    }

    @Test
    void testDetermineRiskLevel_InDanger_OlderMale() {
        String riskLevel = assessmentService.determineRiskLevel(6, 40, "M");
        assertEquals("In danger", riskLevel);
    }

    @Test
    void testDetermineRiskLevel_EarlyOnset_OlderMale() {
        String riskLevel = assessmentService.determineRiskLevel(8, 40, "M");
        assertEquals("Early Onset", riskLevel);
    }

    @Test
    void testDetermineRiskLevel_EarlyOnset_YoungMale() {
        String riskLevel = assessmentService.determineRiskLevel(5, 29, "M");
        assertEquals("Early Onset", riskLevel);
    }

    @Test
    void testDetermineRiskLevel_InDanger_YoungMale() {
        String riskLevel = assessmentService.determineRiskLevel(3, 29, "M");
        assertEquals("In danger", riskLevel);
    }

    @Test
    void testCalculateAge() {
        LocalDate dob = LocalDate.now().minusYears(25);
        int age = assessmentService.calculateAge(dob);
        assertEquals(25, age);
    }

    @Test
    void testGetAllPatients() {
        // Arrange
        String url = "http://localhost:8081/api/patients";

        // Mock the exchange method to return the patients list
        when(restTemplate.exchange(
                eq(url),               // Match the exact URL
                eq(HttpMethod.GET),    // Match the exact HTTP method
                isNull(),              // Match the null body
                eq(new ParameterizedTypeReference<List<Patient>>() {}) // Match the exact ParameterizedTypeReference
        )).thenReturn(new org.springframework.http.ResponseEntity<>(patients, org.springframework.http.HttpStatus.OK));

        // Act
        List<Patient> result = assessmentService.getAllPatients();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Doe", result.get(0).getFamily());
        assertEquals("Smith", result.get(1).getFamily());
        verify(restTemplate, times(1)).exchange(
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                eq(new ParameterizedTypeReference<List<Patient>>() {})
        );
    }

    @Test
    void testGetAllPatients_EmptyList() {
        // Arrange
        String url = "http://localhost:8081/api/patients";  // Fix the URL to match the actual endpoint

        // Mock the exchange method to return an empty list
        when(restTemplate.exchange(
                eq(url),  // Ensure the exact URL is matched
                eq(HttpMethod.GET),  // Ensure the exact HTTP method is matched
                isNull(),  // Ensure that the body is null
                eq(new ParameterizedTypeReference<List<Patient>>() {})  // Ensure the exact ParameterizedTypeReference is matched
        )).thenReturn(new org.springframework.http.ResponseEntity<>(Arrays.asList(), org.springframework.http.HttpStatus.OK));

        // Act
        List<Patient> result = assessmentService.getAllPatients();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());  // Ensure the result is empty
        verify(restTemplate, times(1)).exchange(  // Verify that exchange was called once
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                eq(new ParameterizedTypeReference<List<Patient>>() {})
        );
    }

    @Test
    void testGetAllPatients_Failure() {
        // Arrange
        String url = "http://localhost:8081/api/patients";  // Fix the URL to match the actual endpoint

        // Mock the exchange method to simulate an error (for example, HTTP 500)
        when(restTemplate.exchange(
                eq(url),  // Ensure the exact URL is matched
                eq(HttpMethod.GET),  // Ensure the exact HTTP method is matched
                isNull(),  // Ensure the body is null
                eq(new ParameterizedTypeReference<List<Patient>>() {})  // Ensure the exact ParameterizedTypeReference is matched
        )).thenThrow(new RuntimeException("Failed to retrieve patients"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            assessmentService.getAllPatients();
        });

        assertEquals("Failed to retrieve patients", exception.getMessage());  // Ensure the error message is correct
        verify(restTemplate, times(1)).exchange(  // Verify that exchange was called once
                eq(url),
                eq(HttpMethod.GET),
                isNull(),
                eq(new ParameterizedTypeReference<List<Patient>>() {})
        );
    }


}
