package com.openclassrooms.diabetesAssessment.controller;

import com.openclassrooms.diabetesAssessment.entity.Patient;
import com.openclassrooms.diabetesAssessment.service.AssessmentService;
import com.openclassrooms.diabetesAssessment.controller.ThymeleafPatientController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(ThymeleafPatientController.class)
class ThymeleafPatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssessmentService assessmentService;

    @Test
    void testListAllPatients() throws Exception {
        // Prepare mock data
        Patient patient = new Patient(1L, "John", "Doe", LocalDate.of(2000, 1, 1), "M", 24, "None");
        given(assessmentService.getAllPatients()).willReturn(List.of(patient));  // Mock the service call

        // Perform GET request to /patients
        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(view().name("patients"))
                .andExpect(model().attributeExists("patients"))
                .andExpect(model().attribute("patients", List.of(patient)))
                .andReturn();
    }

    // Test for getting patient assessment by ID
    @Test
    public void testGetPatientAssessmentById() throws Exception {
        Long patientId = 1L;
        String assessmentResult = "Patient: John Doe (age 30) diabetes assessment is: None";

        // Mocking the service method for assessment by ID
        when(assessmentService.assessRiskById(patientId)).thenReturn(assessmentResult);

        mockMvc.perform(get("/patients/{id}/assessment", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("assessment"))
                .andExpect(model().attribute("assessmentResult", assessmentResult));
    }

    // Test for getting patient assessment by full name (family and given name)
    @Test
    public void testGetPatientAssessmentByFullName() throws Exception {
        String familyName = "Doe";
        String givenName = "John";
        String assessmentResult = "Patient: John Doe (age 30) diabetes assessment is: None";

        // Mocking the service method for assessment by name
        when(assessmentService.assessRiskByName(familyName, givenName)).thenReturn(assessmentResult);

        mockMvc.perform(get("/patients/name/{family}/{given}/assessment", familyName, givenName))
                .andExpect(status().isOk())
                .andExpect(view().name("assessment"))
                .andExpect(model().attribute("assessmentResult", assessmentResult));
    }

    // Test when the assessment fails (e.g., patient not found)
    @Test
    public void testGetPatientAssessmentById_Failure() throws Exception {
        Long patientId = 1L;

        // Mocking the service to throw an exception
        when(assessmentService.assessRiskById(patientId)).thenThrow(new RuntimeException("Patient not found"));

        mockMvc.perform(get("/patients/{id}/assessment", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("assessment"))
                .andExpect(model().attribute("errorMessage", "Patient assessment not found or an error occurred."));
    }

    // Test when the assessment fails by full name
    @Test
    public void testGetPatientAssessmentByFullName_Failure() throws Exception {
        String familyName = "Doe";
        String givenName = "John";

        // Mocking the service to throw an exception
        when(assessmentService.assessRiskByName(familyName, givenName)).thenThrow(new RuntimeException("Patient not found"));

        mockMvc.perform(get("/patients/name/{family}/{given}/assessment", familyName, givenName))
                .andExpect(status().isOk())
                .andExpect(view().name("assessment"))
                .andExpect(model().attribute("errorMessage", "Patient assessment not found or an error occurred."));
    }
}