package com.example.tdg.service;

import com.example.tdg.exception.DataGenerationException;
import com.example.tdg.exception.TemplateNotFoundException;
import com.example.tdg.model.dto.BatchGenerationResultDto;
import com.example.tdg.model.entity.Template;
import com.example.tdg.repository.TemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service for batch data generation.
 */
@Service
public class BatchGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(BatchGenerationService.class);
    
    @Autowired
    private DataGenerationService dataGenerationService;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    // Thread pool for parallel generation
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors())
    );
    
    /**
     * Generate data for multiple templates in a batch.
     * 
     * @param templateIds List of template IDs
     * @param rowCount Number of rows per template
     * @param outputFormat Output format
     * @param parallel Whether to generate in parallel
     * @return List of batch generation results
     * @throws TemplateNotFoundException If any template is not found
     * @throws DataGenerationException If generation fails
     */
    public List<BatchGenerationResultDto> generateBatch(
            List<Long> templateIds,
            Integer rowCount,
            Template.OutputFormat outputFormat,
            boolean parallel) throws TemplateNotFoundException, DataGenerationException {
        
        if (templateIds == null || templateIds.isEmpty()) {
            throw new IllegalArgumentException("Template IDs list cannot be empty");
        }
        
        // Set defaults if not provided
        int rows = rowCount != null ? rowCount : 100;
        Template.OutputFormat format = outputFormat != null ? outputFormat : Template.OutputFormat.CSV;
        
        logger.info("Starting batch generation for {} templates, parallel: {}", templateIds.size(), parallel);
        
        // Verify all templates exist before starting batch
        List<Template> templates = templateRepository.findAllById(templateIds);
        if (templates.size() != templateIds.size()) {
            List<Long> foundIds = templates.stream()
                    .map(Template::getId)
                    .collect(Collectors.toList());
            
            List<Long> missingIds = templateIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            
            throw new TemplateNotFoundException("Templates not found with IDs: " + missingIds);
        }
        
        // Sequential or parallel generation based on flag
        if (parallel) {
            return generateParallel(templateIds, rows, format);
        } else {
            return generateSequential(templateIds, rows, format);
        }
    }
    
    /**
     * Generate data sequentially.
     */
    private List<BatchGenerationResultDto> generateSequential(
            List<Long> templateIds,
            int rowCount,
            Template.OutputFormat outputFormat) throws DataGenerationException {
        
        List<BatchGenerationResultDto> results = new ArrayList<>();
        
        for (Long templateId : templateIds) {
            try {
                Instant start = Instant.now();
                
                // Generate data
                byte[] data = dataGenerationService.generateData(templateId, rowCount, outputFormat);
                
                Instant end = Instant.now();
                Duration duration = Duration.between(start, end);
                
                // Create result
                BatchGenerationResultDto result = new BatchGenerationResultDto();
                result.setTemplateId(templateId);
                result.setSuccess(true);
                result.setMessage("Generated " + rowCount + " rows");
                result.setDurationMillis(duration.toMillis());
                result.setOutputFormat(outputFormat);
                result.setDataSize(data.length);
                
                // Store a preview of the data (first few lines)
                String preview = extractPreview(data, outputFormat);
                result.setDataPreview(preview);
                
                results.add(result);
                
                logger.info("Template {} generation completed in {} ms", templateId, duration.toMillis());
            } catch (Exception e) {
                logger.error("Error generating data for template {}: {}", templateId, e.getMessage(), e);
                
                // Create error result
                BatchGenerationResultDto result = new BatchGenerationResultDto();
                result.setTemplateId(templateId);
                result.setSuccess(false);
                result.setMessage("Error: " + e.getMessage());
                results.add(result);
            }
        }
        
        return results;
    }
    
    /**
     * Generate data in parallel.
     */
    private List<BatchGenerationResultDto> generateParallel(
            List<Long> templateIds,
            int rowCount,
            Template.OutputFormat outputFormat) {
        
        List<CompletableFuture<BatchGenerationResultDto>> futures = templateIds.stream()
                .map(templateId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        Instant start = Instant.now();
                        
                        // Generate data
                        byte[] data = dataGenerationService.generateData(templateId, rowCount, outputFormat);
                        
                        Instant end = Instant.now();
                        Duration duration = Duration.between(start, end);
                        
                        // Create result
                        BatchGenerationResultDto result = new BatchGenerationResultDto();
                        result.setTemplateId(templateId);
                        result.setSuccess(true);
                        result.setMessage("Generated " + rowCount + " rows");
                        result.setDurationMillis(duration.toMillis());
                        result.setOutputFormat(outputFormat);
                        result.setDataSize(data.length);
                        
                        // Store a preview of the data (first few lines)
                        String preview = extractPreview(data, outputFormat);
                        result.setDataPreview(preview);
                        
                        logger.info("Template {} generation completed in {} ms", templateId, duration.toMillis());
                        
                        return result;
                    } catch (Exception e) {
                        logger.error("Error generating data for template {}: {}", templateId, e.getMessage(), e);
                        
                        // Create error result
                        BatchGenerationResultDto result = new BatchGenerationResultDto();
                        result.setTemplateId(templateId);
                        result.setSuccess(false);
                        result.setMessage("Error: " + e.getMessage());
                        
                        return result;
                    }
                }, executorService))
                .collect(Collectors.toList());
        
        // Wait for all tasks to complete
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }
    
    /**
     * Extract a preview of the generated data.
     * 
     * @param data The generated data
     * @param outputFormat The output format
     * @return Preview string
     */
    private String extractPreview(byte[] data, Template.OutputFormat outputFormat) {
        String dataString = new String(data, StandardCharsets.UTF_8);
        
        // For each format, extract first few lines or elements
        switch (outputFormat) {
            case CSV:
                // Extract first few lines (header + a few data rows)
                return extractLines(dataString, 5);
            
            case JSON:
                // For JSON, extract the first few array elements
                return extractJsonPreview(dataString, 3);
            
            case XML:
                // For XML, extract the XML declaration and first few rows
                return extractXmlPreview(dataString, 3);
            
            default:
                // Default to first 500 characters
                return dataString.length() <= 500 ? dataString : dataString.substring(0, 500) + "...";
        }
    }
    
    /**
     * Extract first N lines from a string.
     */
    private String extractLines(String text, int lines) {
        String[] allLines = text.split("\n");
        int linesToShow = Math.min(lines, allLines.length);
        
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < linesToShow; i++) {
            preview.append(allLines[i]).append("\n");
        }
        
        if (allLines.length > linesToShow) {
            preview.append("...\n");
        }
        
        return preview.toString();
    }
    
    /**
     * Extract a preview of JSON data.
     */
    private String extractJsonPreview(String json, int elements) {
        // Very simple approach: find first few closing braces for array elements
        // This is not a proper JSON parser but works for our generated JSON
        
        int openBracketPos = json.indexOf('[');
        if (openBracketPos == -1) {
            return json.length() <= 500 ? json : json.substring(0, 500) + "...";
        }
        
        int pos = openBracketPos;
        int elementsFound = 0;
        
        for (int i = openBracketPos + 1; i < json.length(); i++) {
            if (json.charAt(i) == '}') {
                elementsFound++;
                pos = i;
                
                if (elementsFound >= elements) {
                    // Look for the next comma or closing bracket
                    for (int j = i + 1; j < json.length(); j++) {
                        if (json.charAt(j) == ',' || json.charAt(j) == ']') {
                            pos = j;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        // Include closing bracket if near
        int closingBracketPos = json.lastIndexOf(']');
        if (closingBracketPos != -1 && closingBracketPos - pos < 10) {
            pos = closingBracketPos;
        }
        
        return json.substring(0, pos + 1) + (pos + 1 < json.length() ? "..." : "");
    }
    
    /**
     * Extract a preview of XML data.
     */
    private String extractXmlPreview(String xml, int elements) {
        // Simple approach: find first few closing row tags
        // This is not a proper XML parser but works for our generated XML
        
        int rowsFound = 0;
        int pos = 0;
        
        while (rowsFound < elements && pos < xml.length()) {
            int closeRowPos = xml.indexOf("</row>", pos);
            if (closeRowPos == -1) {
                break;
            }
            
            pos = closeRowPos + 6; // Length of "</row>"
            rowsFound++;
        }
        
        // Find the closing root tag
        int closingRootPos = xml.lastIndexOf("</");
        
        if (closingRootPos != -1 && xml.length() - closingRootPos < 50) {
            // If the closing tag is near, include it
            return xml.substring(0, xml.length());
        } else if (pos < xml.length()) {
            // Otherwise truncate with ellipsis
            return xml.substring(0, pos) + "\n...";
        } else {
            // If we processed the whole string, return it
            return xml;
        }
    }
}
