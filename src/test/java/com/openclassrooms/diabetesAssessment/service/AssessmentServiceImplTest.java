package com.openclassrooms.diabetesAssessment.service;

import com.openclassrooms.diabetesAssessment.entity.Patient;
import com.openclassrooms.diabetesAssessment.entity.PatientNotesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssessmentServiceImplTest {

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    @Mock
    private RestTemplate restTemplate;

    private Patient patient;
    private PatientNotesResponse notesResponse;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setPatientId(1L);
        patient.setFamily("Doe");
        patient.setGivenName("John");
        patient.setDateOfBirth(LocalDate.of(1985, 5, 15));
        patient.setSex("M");

        notesResponse = new PatientNotesResponse();
        notesResponse.setNote(List.of("Patient has Hemoglobin A1C levels high", "Shows signs of Dizziness"));
    }

    @Test
    void testAssessRiskById_Success() {
        // Arrange
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setPatientId(patientId);
        patient.setFamily("Doe");
        patient.setGivenName("John");
        patient.setDateOfBirth(LocalDate.of(1985, 5, 15)); // Set a sample DOB
        patient.setSex("M"); // Set a sample sex

        // Mock the REST call to return the patient
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(patient);

        // Mock the notes response
        PatientNotesResponse notesResponse = new PatientNotesResponse();
        notesResponse.setNote(List.of("Body Weight", "Smoker")); // Example notes that trigger a risk
        when(restTemplate.getForObject(anyString(), eq(PatientNotesResponse.class))).thenReturn(notesResponse);

        // Act
        String result = assessmentService.assessRiskById(patientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("diabetes assessment is: Borderline")); // Update based on expected output
    }


    @Test
    void testAssessRiskById_PatientNotFound() {
        // Arrange
        Long patientId = 2L;
        // Mock the REST call to return null, simulating patient not found
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            assessmentService.assessRiskById(patientId);
        });

        assertEquals("Patient not found with ID: " + patientId, exception.getMessage());
    }


    @Test
    void testAssessRiskByName_Success() {
        // Arrange
        Patient patient = new Patient();
        patient.setPatientId(1L);
        patient.setFamily("Doe");
        patient.setGivenName("John");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1)); // Setting a valid DOB

        // Mock the patient response from the REST call
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(patient);

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

        // Mock the patient response to be null, simulating patient not found
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(null);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            assessmentService.assessRiskByName(familyName, givenName);
        });
    }

    @Test
    void testAssessRiskByName_NoDateOfBirth() {
        // Arrange
        Patient patient = new Patient();
        patient.setPatientId(1L);
        patient.setFamily("Doe");
        patient.setGivenName("John");
        patient.setDateOfBirth(null); // No DOB

        // Mock the patient response from the REST call
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(patient);

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
    void testCalculateAge() {
        LocalDate dob = LocalDate.now().minusYears(25);
        int age = assessmentService.calculateAge(dob);
        assertEquals(25, age);
    }
}
