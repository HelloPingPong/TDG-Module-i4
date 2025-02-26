package com.example.tdg.service.generator.types;

import com.example.tdg.service.generator.AbstractDataGenerator;
import com.example.tdg.service.generator.DataGeneratorType;
import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates random first names using Java Faker.
 */
@Component
@DataGeneratorType(
    category = "Personal",
    displayName = "First Name",
    description = "Generates random first names"
)
public class FirstNameGenerator extends AbstractDataGenerator {
    
    private final Faker faker;
    
    public FirstNameGenerator() {
        super("firstName");
        this.faker = new Faker();
    }
    
    @Override
    public String generate(Map<String, Object> constraints) {
        String gender = getConstraint(constraints, "gender", "any");
        
        switch (gender.toLowerCase()) {
            case "male":
                return faker.name().firstName().replace(".", ""); // Avoid abbreviations
            case "female":
                return faker.name().firstName().replace(".", "");
            default:
                return faker.name().firstName().replace(".", "");
        }
    }
    
    @Override
    public Map<String, String> getConstraintsMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("gender", "select:any,male,female");
        return metadata;
    }
}
