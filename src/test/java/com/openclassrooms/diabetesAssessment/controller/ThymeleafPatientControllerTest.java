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

    @Test
    void testGetPatientAssessment() throws Exception {
        Long patientId = 1L;
        String assessmentResult = "Patient: John Doe (age 24) diabetes assessment is: None";
        given(assessmentService.assessRiskById(patientId)).willReturn(assessmentResult);

        // Perform GET request to /patients/{id}/assessment
        mockMvc.perform(get("/patients/{id}/assessment", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("assessment"))
                .andExpect(model().attribute("assessmentResult", assessmentResult))
                .andReturn();
    }

    @Test
    void testGetPatientAssessment_NotFound() throws Exception {
        Long patientId = 999L;
        given(assessmentService.assessRiskById(patientId)).willThrow(new RuntimeException("Patient not found"));

        // Perform GET request to /patients/{id}/assessment with a non-existing patient ID
        mockMvc.perform(get("/patients/{id}/assessment", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("assessment"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Patient assessment not found or an error occurred."))
                .andReturn();
    }
}
