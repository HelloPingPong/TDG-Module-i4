package com.example.tdg.service.generator;

import java.util.Map;
import java.util.Optional;

/**
 * Interface for all data generators.
 * Each implementation will handle a specific type of data generation (e.g., names, dates, numbers).
 */
public interface DataGenerator {
    
    /**
     * Generates data based on provided constraints.
     * 
     * @param constraints Map of constraint name to constraint value
     * @return Generated data as string
     */
    String generate(Map<String, Object> constraints);
    
    /**
     * Returns the type of data this generator produces.
     * This is used for registration and lookup in the generator registry.
     * 
     * @return The data type as a string (e.g., "firstName", "zipCode")
     */
    String getType();
    
    /**
     * Returns metadata about supported constraints for this generator.
     * This is used by the UI to dynamically build constraint forms.
     * 
     * @return Map of constraint name to constraint type (e.g., "minLength" -> "number")
     */
    Map<String, String> getConstraintsMetadata();
    
    /**
     * Validates whether the provided constraints are valid for this generator.
     * 
     * @param constraints Map of constraint name to constraint value
     * @return Optional with error message if invalid, empty if valid
     */
    Optional<String> validateConstraints(Map<String, Object> constraints);
}
