package com.openclassrooms.diabetesAssessment.controller;

import com.openclassrooms.diabetesAssessment.service.AssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientControllerTest {

    @InjectMocks
    private PatientController patientController;

    @Mock
    private AssessmentService assessmentService;

    @BeforeEach
    public void setUp() {
        // This method can be used for initialization if needed
    }

    @Test
    public void testAssessPatient_Found() {
        Long patId = 1L;
        String riskAssessment = "Low risk";

        // Mock the service response for a valid patient ID
        when(assessmentService.assessRiskById(patId)).thenReturn(riskAssessment);

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatient(patId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(riskAssessment, response.getBody());
        verify(assessmentService, times(1)).assessRiskById(patId);
    }

    @Test
    public void testAssessPatient_NotFound() {
        Long patId = 1L;

        // Mock the service response to return "Patient not found"
        when(assessmentService.assessRiskById(patId)).thenReturn("Patient not found");

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatient(patId);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Patient not found", response.getBody());
        verify(assessmentService, times(1)).assessRiskById(patId);
    }

    @Test
    public void testAssessPatientByName_Found() {
        String familyName = "Doe";
        String givenName = "John";
        String riskAssessment = "Moderate risk";

        // Mock the service response for valid names
        when(assessmentService.assessRiskByName(familyName, givenName)).thenReturn(riskAssessment);

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatientByName(familyName, givenName);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(riskAssessment, response.getBody());
        verify(assessmentService, times(1)).assessRiskByName(familyName, givenName);
    }

    @Test
    public void testAssessPatientByName_NotFound() {
        String familyName = "Doe";
        String givenName = "Jane";

        // Mock the service response to return "Patient not found"
        when(assessmentService.assessRiskByName(familyName, givenName)).thenReturn("Patient not found");

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatientByName(familyName, givenName);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Patient not found", response.getBody());
        verify(assessmentService, times(1)).assessRiskByName(familyName, givenName);
    }
}
