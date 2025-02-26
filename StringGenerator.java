package com.example.tdg.service.generator.types;

import com.example.tdg.service.generator.AbstractDataGenerator;
import com.example.tdg.service.generator.DataGeneratorType;
import com.mifmif.common.regex.Generex;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Generates random strings based on length constraints or regex patterns.
 */
@Component
@DataGeneratorType(
    category = "Text",
    displayName = "String",
    description = "Generates random text strings with configurable length or pattern"
)
public class StringGenerator extends AbstractDataGenerator {
    
    private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String ALPHANUMERIC = ALPHA + "0123456789";
    private final Random random = new Random();
    
    public StringGenerator() {
        super("string");
    }
    
    @Override
    public String generate(Map<String, Object> constraints) {
        // Check if pattern is provided
        String pattern = getConstraint(constraints, "pattern", "");
        if (pattern != null && !pattern.isEmpty()) {
            try {
                Generex generex = new Generex(pattern);
                return generex.random();
            } catch (Exception e) {
                // Fall back to length-based generation if pattern is invalid
            }
        }
        
        // Length-based generation
        int minLength = getConstraint(constraints, "minLength", 5);
        int maxLength = getConstraint(constraints, "maxLength", 10);
        
        // Ensure valid range
        if (minLength < 0) minLength = 0;
        if (maxLength < minLength) maxLength = minLength;
        
        // Determine character set
        boolean alphaOnly = getConstraint(constraints, "alphaOnly", false);
        String charSet = alphaOnly ? ALPHA : ALPHANUMERIC;
        
        // Generate random length between min and max
        int length = minLength;
        if (maxLength > minLength) {
            length = minLength + random.nextInt(maxLength - minLength + 1);
        }
        
        // Generate random string
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charSet.length());
            sb.append(charSet.charAt(index));
        }
        
        return sb.toString();
    }
    
    @Override
    public Map<String, String> getConstraintsMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("pattern", "string");
        metadata.put("minLength", "number");
        metadata.put("maxLength", "number");
        metadata.put("alphaOnly", "boolean");
        return metadata;
    }
    
    @Override
    public Optional<String> validateConstraints(Map<String, Object> constraints) {
        // Validate pattern if provided
        String pattern = getConstraint(constraints, "pattern", "");
        if (pattern != null && !pattern.isEmpty()) {
            try {
                new Generex(pattern);
            } catch (Exception e) {
                return Optional.of("Invalid regex pattern: " + e.getMessage());
            }
        }
        
        // Validate length constraints
        Integer minLength = getConstraint(constraints, "minLength", null);
        Integer maxLength = getConstraint(constraints, "maxLength", null);
        
        if (minLength != null && minLength < 0) {
            return Optional.of("Minimum length cannot be negative");
        }
        
        if (minLength != null && maxLength != null && maxLength < minLength) {
            return Optional.of("Maximum length cannot be less than minimum length");
        }
        
        return Optional.empty();
    }
}
