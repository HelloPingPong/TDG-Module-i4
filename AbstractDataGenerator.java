package com.example.tdg.service.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base class for data generators.
 * Provides common functionality and default implementations.
 */
public abstract class AbstractDataGenerator implements DataGenerator {
    
    private final String type;
    
    protected AbstractDataGenerator(String type) {
        this.type = type;
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public Map<String, String> getConstraintsMetadata() {
        // Default implementation returns empty map
        // Override in subclasses to provide constraints
        return new HashMap<>();
    }
    
    @Override
    public Optional<String> validateConstraints(Map<String, Object> constraints) {
        // Default implementation accepts all constraints
        // Override in subclasses for specific validation
        return Optional.empty();
    }
    
    /**
     * Helper method to get a constraint value with type casting.
     * 
     * @param constraints The constraints map
     * @param key The constraint key
     * @param defaultValue Default value if constraint is not present
     * @param <T> Type of the constraint value
     * @return The constraint value or default if not present
     */
    @SuppressWarnings("unchecked")
    protected <T> T getConstraint(Map<String, Object> constraints, String key, T defaultValue) {
        Object value = constraints.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }
}
