package com.example.tdg.service;

import com.example.tdg.exception.DataGenerationException;
import com.example.tdg.exception.TemplateNotFoundException;
import com.example.tdg.model.entity.ColumnDefinition;
import com.example.tdg.model.entity.Template;
import com.example.tdg.repository.TemplateRepository;
import com.example.tdg.service.generator.DataGenerator;
import com.example.tdg.service.generator.DataTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service for generating data based on templates.
 */
@Service
public class DataGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataGenerationService.class);
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private DataTypeRegistry dataTypeRegistry;
    
    /**
     * Generate data based on a template.
     * 
     * @param templateId The template ID
     * @param rowCount The number of rows to generate
     * @param outputFormat The output format (CSV, JSON, XML)
     * @return Generated data as byte array
     * @throws TemplateNotFoundException If template not found
     * @throws DataGenerationException If generation fails
     */
    public byte[] generateData(Long templateId, int rowCount, Template.OutputFormat outputFormat) 
            throws TemplateNotFoundException, DataGenerationException {
        
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + templateId));
        
        // Generate data rows
        List<Map<String, String>> dataRows = generateDataRows(template, rowCount);
        
        // Format output
        return formatOutput(dataRows, template, outputFormat);
    }
    
    /**
     * Generate data rows based on template column definitions.
     * 
     * @param template The template
     * @param rowCount The number of rows to generate
     * @return List of data rows (each row is a map of column name to value)
     * @throws DataGenerationException If generation fails
     */
    private List<Map<String, String>> generateDataRows(Template template, int rowCount) throws DataGenerationException {
        List<ColumnDefinition> columnDefinitions = template.getColumnDefinitions().stream()
                .sorted(Comparator.comparing(ColumnDefinition::getSequenceNumber))
                .collect(Collectors.toList());
        
        return IntStream.range(0, rowCount)
                .mapToObj(i -> generateRow(columnDefinitions))
                .collect(Collectors.toList());
    }
    
    /**
     * Generate a single data row.
     * 
     * @param columnDefinitions The column definitions
     * @return Map of column name to generated value
     * @throws DataGenerationException If generation fails
     */
    private Map<String, String> generateRow(List<ColumnDefinition> columnDefinitions) throws DataGenerationException {
        Map<String, String> row = new LinkedHashMap<>();
        
        for (ColumnDefinition column : columnDefinitions) {
            String value = generateValue(column);
            row.put(column.getName(), value);
        }
        
        return row;
    }
    
    /**
     * Generate a value for a single column.
     * 
     * @param column The column definition
     * @return Generated value
     * @throws DataGenerationException If generation fails
     */
    private String generateValue(ColumnDefinition column) throws DataGenerationException {
        // Check if column should be null
        if (column.getIsNullable() && Math.random() < column.getNullProbability()) {
            return null;
        }
        
        String type = column.getType();
        Optional<DataGenerator> generatorOpt = dataTypeRegistry.getGenerator(type);
        
        if (!generatorOpt.isPresent()) {
            throw new DataGenerationException("No generator found for type: " + type);
        }
        
        DataGenerator generator = generatorOpt.get();
        
        // Convert string constraints to appropriate types
        Map<String, Object> typedConstraints = new HashMap<>();
        for (Map.Entry<String, String> entry : column.getConstraints().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Try to convert to appropriate type
            try {
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    typedConstraints.put(key, Boolean.parseBoolean(value));
                } else if (value.matches("\\d+")) {
                    typedConstraints.put(key, Integer.parseInt(value));
                } else if (value.matches("\\d+\\.\\d+")) {
                    typedConstraints.put(key, Double.parseDouble(value));
                } else {
                    typedConstraints.put(key, value);
                }
            } catch (Exception e) {
                // If parsing fails, use the original string value
                typedConstraints.put(key, value);
            }
        }
        
        try {
            return generator.generate(typedConstraints);
        } catch (Exception e) {
            logger.error("Error generating value for column {}: {}", column.getName(), e.getMessage(), e);
            throw new DataGenerationException("Error generating value for column " + column.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Format the generated data in the specified output format.
     * 
     * @param dataRows The generated data rows
     * @param template The template
     * @param outputFormat The output format
     * @return Formatted data as byte array
     * @throws DataGenerationException If formatting fails
     */
    private byte[] formatOutput(List<Map<String, String>> dataRows, Template template, Template.OutputFormat outputFormat) 
            throws DataGenerationException {
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            switch (outputFormat) {
                case CSV:
                    formatCsv(dataRows, outputStream);
                    break;
                case JSON:
                    formatJson(dataRows, outputStream);
                    break;
                case XML:
                    formatXml(dataRows, template.getName(), outputStream);
                    break;
                default:
                    throw new DataGenerationException("Unsupported output format: " + outputFormat);
            }
            
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new DataGenerationException("Error formatting output: " + e.getMessage());
        }
    }
    
    /**
     * Format data as CSV.
     * 
     * @param dataRows The data rows
     * @param outputStream The output stream
     * @throws IOException If writing fails
     */
    private void formatCsv(List<Map<String, String>> dataRows, ByteArrayOutputStream outputStream) throws IOException {
        if (dataRows.isEmpty()) {
            return;
        }
        
        // Write header
        Set<String> headers = dataRows.get(0).keySet();
        outputStream.write(String.join(",", headers).getBytes(StandardCharsets.UTF_8));
        outputStream.write('\n');
        
        // Write rows
        for (Map<String, String> row : dataRows) {
            List<String> values = new ArrayList<>();
            
            for (String header : headers) {
                String value = row.get(header);
                
                // Handle null values and escaping
                if (value == null) {
                    values.add("");
                } else if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                    // Escape quotes by doubling them and wrap in quotes
                    String escaped = value.replace("\"", "\"\"");
                    values.add("\"" + escaped + "\"");
                } else {
                    values.add(value);
                }
            }
            
            outputStream.write(String.join(",", values).getBytes(StandardCharsets.UTF_8));
            outputStream.write('\n');
        }
    }
    
    /**
     * Format data as JSON.
     * 
     * @param dataRows The data rows
     * @param outputStream The output stream
     * @throws IOException If writing fails
     */
    private void formatJson(List<Map<String, String>> dataRows, ByteArrayOutputStream outputStream) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[\n");
        
        for (int i = 0; i < dataRows.size(); i++) {
            Map<String, String> row = dataRows.get(i);
            json.append("  {\n");
            
            int j = 0;
            for (Map.Entry<String, String> entry : row.entrySet()) {
                json.append("    \"").append(entry.getKey()).append("\": ");
                
                if (entry.getValue() == null) {
                    json.append("null");
                } else {
                    json.append("\"").append(escapeJsonString(entry.getValue())).append("\"");
                }
                
                if (j < row.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
                j++;
            }
            
            json.append("  }");
            if (i < dataRows.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("]\n");
        outputStream.write(json.toString().getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Escape special characters in JSON string.
     * 
     * @param input The input string
     * @return Escaped string
     */
    private String escapeJsonString(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * Format data as XML.
     * 
     * @param dataRows The data rows
     * @param rootElementName The name of the root element
     * @param outputStream The output stream
     * @throws IOException If writing fails
     */
    private void formatXml(List<Map<String, String>> dataRows, String rootElementName, ByteArrayOutputStream outputStream) 
            throws IOException {
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        // Sanitize root element name for XML
        String sanitizedRootName = sanitizeXmlName(rootElementName);
        
        xml.append("<").append(sanitizedRootName).append(">\n");
        
        for (Map<String, String> row : dataRows) {
            xml.append("  <row>\n");
            
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String columnName = sanitizeXmlName(entry.getKey());
                xml.append("    <").append(columnName).append(">");
                
                if (entry.getValue() != null) {
                    xml.append(escapeXmlString(entry.getValue()));
                }
                
                xml.append("</").append(columnName).append(">\n");
            }
            
            xml.append("  </row>\n");
        }
        
        xml.append("</").append(sanitizedRootName).append(">\n");
        outputStream.write(xml.toString().getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * Sanitize a string for use as an XML name.
     * 
     * @param name The input name
     * @return Sanitized name
     */
    private String sanitizeXmlName(String name) {
        // XML names must start with a letter or underscore
        String sanitized = name.replaceAll("[^a-zA-Z0-9_.-]", "_");
        
        // If first character is not a letter or underscore, prepend underscore
        if (!sanitized.matches("^[a-zA-Z_].*")) {
            sanitized = "_" + sanitized;
        }
        
        return sanitized;
    }
    
    /**
     * Escape special characters in XML string.
     * 
     * @param input The input string
     * @return Escaped string
     */
    private String escapeXmlString(String input) {
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
