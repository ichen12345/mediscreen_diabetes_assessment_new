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
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityNotFoundException;

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
    void testAssessPatient_Success() {
        Long patId = 1L;
        String expectedResult = "Assessment result here"; // Expected result from the service

        // Mock the service call to return a successful result
        when(assessmentService.assessRiskById(patId)).thenReturn(expectedResult);

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatient(patId);

        // Verify the results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        verify(assessmentService, times(1)).assessRiskById(patId);
    }

    @Test
    void testAssessPatient_PatientNotFound() {
        Long patId = 2L;

        // Mock the service call to throw an EntityNotFoundException
        when(assessmentService.assessRiskById(patId)).thenThrow(new EntityNotFoundException("Patient not found with ID: " + patId));

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatient(patId);

        // Verify the results
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Patient not found", response.getBody());
        verify(assessmentService, times(1)).assessRiskById(patId);
    }

    @Test
    void testAssessPatient_HttpClientError() {
        Long patId = 3L;

        // Mock the service call to throw an HttpClientErrorException
        when(assessmentService.assessRiskById(patId)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatient(patId);

        // Verify the results
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Patient not found", response.getBody());
        verify(assessmentService, times(1)).assessRiskById(patId);
    }

    @Test
    void testAssessPatient_InternalServerError() {
        Long patId = 4L;

        // Mock the service call to throw a general exception
        when(assessmentService.assessRiskById(patId)).thenThrow(new RuntimeException("Unexpected error"));

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatient(patId);

        // Verify the results
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred", response.getBody());
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

        // Mock the service to throw an EntityNotFoundException
        when(assessmentService.assessRiskByName(familyName, givenName)).thenThrow(new EntityNotFoundException("Patient not found"));

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatientByName(familyName, givenName);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Patient not found", response.getBody());
        verify(assessmentService, times(1)).assessRiskByName(familyName, givenName);
    }

    @Test
    public void testAssessPatientByName_InternalServerError() {
        String familyName = "Doe";
        String givenName = "Unknown";

        // Mock the service to throw a generic Exception
        when(assessmentService.assessRiskByName(familyName, givenName)).thenThrow(new RuntimeException("Unexpected error"));

        // Call the controller method
        ResponseEntity<String> response = patientController.assessPatientByName(familyName, givenName);

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An error occurred", response.getBody());
        verify(assessmentService, times(1)).assessRiskByName(familyName, givenName);
    }

}
