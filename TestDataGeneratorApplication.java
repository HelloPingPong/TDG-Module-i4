package com.example.tdg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TestDataGeneratorApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestDataGeneratorApplication.class, args);
    }
}
