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
        // Mock the patient response from the REST call
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(patient);

        // Mock the notes response from the REST call
        PatientNotesResponse notesResponse = new PatientNotesResponse();
        notesResponse.setNote(List.of("Body Weight", "Smoker")); // Two triggers
        when(restTemplate.getForObject(anyString(), eq(PatientNotesResponse.class))).thenReturn(notesResponse);

        // Call the method under test
        String result = assessmentService.assessRiskById(1L);

        // Validate the result
        assertNotNull(result);
        assertTrue(result.contains("diabetes assessment is: Borderline"));
    }

    @Test
    void testAssessRiskById_PatientNotFound() {
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(null);

        String result = assessmentService.assessRiskById(1L);

        assertEquals("Patient not found", result);
    }

    @Test
    void testAssessRiskByName_Success() {
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
        when(restTemplate.getForObject(anyString(), eq(Patient.class))).thenReturn(null);

        String result = assessmentService.assessRiskByName("Doe", "John");

        assertEquals("Patient not found", result);
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
