package com.openclassrooms.diabetesAssessment.controller;

import com.openclassrooms.diabetesAssessment.service.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/access")
public class PatientController {
    @Autowired
    AssessmentService assessmentService;

    @PostMapping("/id")
    public ResponseEntity<String> assessPatient(@RequestParam("patId") Long patId) {
        String result = assessmentService.assessRisk(patId);
        if ("Patient not found".equals(result)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found");
        }

        return ResponseEntity.ok(result);
    }

}
