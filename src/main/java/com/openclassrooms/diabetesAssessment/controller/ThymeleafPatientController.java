package com.openclassrooms.diabetesAssessment.controller;

import com.openclassrooms.diabetesAssessment.entity.Patient;
import com.openclassrooms.diabetesAssessment.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ThymeleafPatientController {

    @Autowired
    private AssessmentService assessmentService;

    // Main page listing all patients
    @GetMapping("/patients")
    public String listAllPatients(Model model) {
        List<Patient> patients = assessmentService.getAllPatients(); // Assuming a method to fetch all patients
        model.addAttribute("patients", patients);
        return "patients"; // Renders the "patients.html" view
    }

    // Details page for showing assessment results of a specific patient
    @GetMapping("/patients/{id}/assessment")
    public String getPatientAssessment(@PathVariable("id") Long patId, Model model) {
        try {
            String assessmentResult = assessmentService.assessRiskById(patId);
            model.addAttribute("assessmentResult", assessmentResult);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Patient assessment not found or an error occurred.");
        }
        return "assessment"; // Renders the "assessment.html" view
    }
}

