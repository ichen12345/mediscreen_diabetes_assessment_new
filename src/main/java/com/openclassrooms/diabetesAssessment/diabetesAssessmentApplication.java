package com.openclassrooms.diabetesAssessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class diabetesAssessmentApplication {
	public static void main(String[] args) {
		SpringApplication.run(diabetesAssessmentApplication.class, args);
	}

}
