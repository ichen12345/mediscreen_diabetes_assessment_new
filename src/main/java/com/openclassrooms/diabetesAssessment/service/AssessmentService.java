package com.openclassrooms.diabetesAssessment.service;

import com.openclassrooms.diabetesAssessment.entity.Patient;

public interface AssessmentService {
    String assessRiskById(Long patientId);
    String assessRiskByName(String family, String given);
}
