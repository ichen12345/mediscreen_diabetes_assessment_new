package com.openclassrooms.diabetesAssessment.entity;

import java.util.List;

public class PatientNotesResponse {
    private String id;
    private Long patId;
    private List<String> note;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getPatId() {
        return patId;
    }

    public void setPatId(Long patId) {
        this.patId = patId;
    }

    public List<String> getNote() {
        return note;
    }

    public void setNote(List<String> note) {
        this.note = note;
    }
}
