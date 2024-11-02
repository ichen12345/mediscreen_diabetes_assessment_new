package com.openclassrooms.diabetesAssessment.service;

import com.openclassrooms.diabetesAssessment.entity.Patient;

import java.util.List;

public interface AssessmentService {
    String assessRiskById(Long patientId);
    String assessRiskByName(String family, String given);

    List<Patient> getAllPatients();
}
