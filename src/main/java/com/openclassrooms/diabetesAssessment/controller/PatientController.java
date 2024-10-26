package com.openclassrooms.diabetesAssessment.controller;

import com.openclassrooms.diabetesAssessment.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assess")
public class PatientController {
    @Autowired
    AssessmentService assessmentService;

    @PostMapping("/id")
    public ResponseEntity<String> assessPatient(@RequestParam("patId") Long patId) {
        System.out.println("Received patId: " + patId); // Add this log statement
        String result = assessmentService.assessRiskById(patId);
        if ("Patient not found".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/name")
    public ResponseEntity<String> assessPatientByName(@RequestParam String family, @RequestParam String given) {
        System.out.println("Received family: " + family + ", given: " + given); // Log parameters
        String result = assessmentService.assessRiskByName(family, given);
        if ("Patient not found".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }
        return ResponseEntity.ok(result);
    }

}
