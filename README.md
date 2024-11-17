# Mediscreen Sprint 3 - Diabetes Assessment

This project is a **Spring Boot** application for managing patient diabetes assessments. It allows users to view patients, view their assessment results, and assess risk factors based on predefined triggers.

## Features

- View all patients
- View patient details and assessments
- Perform risk assessment based on medical history
- Risk levels: None, Borderline, In Danger, Early Onset
- Dockerized deployment for easy setup and scalability

## Technologies Used

- Java
- Spring Boot
- Thymeleaf
- Maven
- Docker
- MongoDB
- PostgreSQL
- 
## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/ichen12345/mediscreen_diabetes_assessment_new.git
2. Run `mvn clean install` to build the project
3. (Without Docker) Run the application with `mvn spring-boot:run`/ (With Docker) Run the application with docker using `docker compose up -d`
4. Verify that the application is running: Open your browser and navigate to http://localhost:8083/patients to view the list of patients.

## Usage

1. View the list of all patients by navigating to http://localhost:8083/patients.
2. Click on a patient’s name or id to view detailed information and their diabetes risk assessment.
3. The application automatically calculates the risk level for each patient based on their medical history.


