package com.openclassrooms.diabetesAssessment.controller;

import com.openclassrooms.diabetesAssessment.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/assess")
public class PatientController {
    @Autowired
    AssessmentService assessmentService;

    @PostMapping("/id")
    public ResponseEntity<String> assessPatient(@RequestParam("patId") Long patId) {
        System.out.println("Received patId: " + patId); // Add this log statement
        try {
            String result = assessmentService.assessRiskById(patId);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            // Handle the case when the patient is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        } catch (HttpClientErrorException e) {
            // Handle the case where the client error (e.g., 404) occurs during REST call
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        } catch (Exception e) {
            // General exception handling (optional)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }


    @PostMapping("/name")
    public ResponseEntity<String> assessPatientByName(@RequestParam String family, @RequestParam String given) {
        System.out.println("Received family: " + family + ", given: " + given); // Log parameters
        try {
            String result = assessmentService.assessRiskByName(family, given);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            // Return 404 if patient not found in a specific case
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        } catch (Exception e) {
            // General exception handling
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }
    }



}
