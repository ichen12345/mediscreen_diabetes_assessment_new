package com.openclassrooms.diabetesAssessment.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseInitializer {
    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void init() {
        mongoTemplate.getCollection("patient").drop();
        // Optionally, insert initial data here
    }
}
