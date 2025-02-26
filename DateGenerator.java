package com.example.tdg.service.generator.types;

import com.example.tdg.service.generator.AbstractDataGenerator;
import com.example.tdg.service.generator.DataGeneratorType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates random dates within a specified range and format.
 */
@Component
@DataGeneratorType(
    category = "DateTime",
    displayName = "Date",
    description = "Generates random dates within a specified range"
)
public class DateGenerator extends AbstractDataGenerator {
    
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd";
    private static final LocalDate DEFAULT_MIN_DATE = LocalDate.now().minusYears(5);
    private static final LocalDate DEFAULT_MAX_DATE = LocalDate.now();
    
    public DateGenerator() {
        super("date");
    }
    
    @Override
    public String generate(Map<String, Object> constraints) {
        // Parse min date
        LocalDate minDate = parseDate(
            getConstraint(constraints, "minDate", null),
            DEFAULT_MIN_DATE
        );
        
        // Parse max date
        LocalDate maxDate = parseDate(
            getConstraint(constraints, "maxDate", null),
            DEFAULT_MAX_DATE
        );
        
        // Ensure valid range
        if (maxDate.isBefore(minDate)) {
            maxDate = minDate;
        }
        
        // Generate random date between min and max
        long minDay = minDate.toEpochDay();
        long maxDay = maxDate.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay + 1);
        LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
        
        // Format the date
        String format = getConstraint(constraints, "format", DEFAULT_FORMAT);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return randomDate.format(formatter);
        } catch (IllegalArgumentException e) {
            // Fall back to default format if custom format is invalid
            return randomDate.format(DateTimeFormatter.ofPattern(DEFAULT_FORMAT));
        }
    }
    
    @Override
    public Map<String, String> getConstraintsMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("minDate", "string");
        metadata.put("maxDate", "string");
        metadata.put("format", "string");
        return metadata;
    }
    
    @Override
    public Optional<String> validateConstraints(Map<String, Object> constraints) {
        // Validate date format
        String format = getConstraint(constraints, "format", DEFAULT_FORMAT);
        try {
            DateTimeFormatter.ofPattern(format);
        } catch (IllegalArgumentException e) {
            return Optional.of("Invalid date format: " + e.getMessage());
        }
        
        // Validate min date
        String minDateStr = getConstraint(constraints, "minDate", null);
        if (minDateStr != null) {
            try {
                LocalDate.parse(minDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                return Optional.of("Invalid minDate: Format should be yyyy-MM-dd");
            }
        }
        
        // Validate max date
        String maxDateStr = getConstraint(constraints, "maxDate", null);
        if (maxDateStr != null) {
            try {
                LocalDate.parse(maxDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e) {
                return Optional.of("Invalid maxDate: Format should be yyyy-MM-dd");
            }
        }
        
        return Optional.empty();
    }
    
    private LocalDate parseDate(String dateStr, LocalDate defaultValue) {
        if (dateStr == null) {
            return defaultValue;
        }
        
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }
}
