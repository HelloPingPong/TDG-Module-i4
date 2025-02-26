package com.example.tdg.controller;

import com.example.tdg.exception.DataGenerationException;
import com.example.tdg.exception.TemplateNotFoundException;
import com.example.tdg.model.dto.GenerationRequestDto;
import com.example.tdg.model.entity.Template;
import com.example.tdg.service.DataGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

/**
 * REST API controller for data generation.
 */
@RestController
@RequestMapping("/api/generate")
public class DataGenerationController {
    
    @Autowired
    private DataGenerationService dataGenerationService;
    
    /**
     * Generate data based on a template.
     * 
     * @param generationRequest The generation request DTO
     * @return The generated data as a file download
     */
    @PostMapping
    public ResponseEntity<byte[]> generateData(@Valid @RequestBody GenerationRequestDto generationRequest) {
        try {
            // Get parameters from request
            Long templateId = generationRequest.getTemplateId();
            int rowCount = generationRequest.getRowCount() != null ? generationRequest.getRowCount() : 100;
            Template.OutputFormat outputFormat = generationRequest.getOutputFormat() != null 
                    ? generationRequest.getOutputFormat() : Template.OutputFormat.CSV;
            
            // Generate the data
            byte[] data = dataGenerationService.generateData(templateId, rowCount, outputFormat);
            
            // Set up file name and content type
            String filename = generationRequest.getFilename();
            if (filename == null || filename.trim().isEmpty()) {
                filename = "generated_data_" + UUID.randomUUID().toString();
            }
            
            // Add appropriate extension based on format
            switch (outputFormat) {
                case CSV:
                    filename += ".csv";
                    break;
                case JSON:
                    filename += ".json";
                    break;
                case XML:
                    filename += ".xml";
                    break;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", filename);
            
            // Set content type based on format
            MediaType mediaType;
            switch (outputFormat) {
                case CSV:
                    mediaType = MediaType.parseMediaType("text/csv");
                    break;
                case JSON:
                    mediaType = MediaType.APPLICATION_JSON;
                    break;
                case XML:
                    mediaType = MediaType.APPLICATION_XML;
                    break;
                default:
                    mediaType = MediaType.TEXT_PLAIN;
            }
            headers.setContentType(mediaType);
            
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (TemplateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataGenerationException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Generate data for a template directly via GET.
     * This is useful for simple generation via browser.
     * 
     * @param templateId The template ID
     * @param rowCount The number of rows to generate (optional)
     * @param format The output format (optional)
     * @return The generated data as a file download
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<byte[]> generateDataGet(
            @PathVariable Long templateId,
            @RequestParam(required = false) Integer rowCount,
            @RequestParam(required = false) String format) {
        
        try {
            // Set defaults if not provided
            int rows = rowCount != null ? rowCount : 100;
            
            Template.OutputFormat outputFormat = Template.OutputFormat.CSV;
            if (format != null) {
                try {
                    outputFormat = Template.OutputFormat.valueOf(format.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // Invalid format, use default
                }
            }
            
            // Generate the data
            byte[] data = dataGenerationService.generateData(templateId, rows, outputFormat);
            
            // Set up file name and content type
            String filename = "generated_data_" + templateId + "_" + UUID.randomUUID().toString();
            
            // Add appropriate extension
            switch (outputFormat) {
                case CSV:
                    filename += ".csv";
                    break;
                case JSON:
                    filename += ".json";
                    break;
                case XML:
                    filename += ".xml";
                    break;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", filename);
            
            // Set content type based on format
            MediaType mediaType;
            switch (outputFormat) {
                case CSV:
                    mediaType = MediaType.parseMediaType("text/csv");
                    break;
                case JSON:
                    mediaType = MediaType.APPLICATION_JSON;
                    break;
                case XML:
                    mediaType = MediaType.APPLICATION_XML;
                    break;
                default:
                    mediaType = MediaType.TEXT_PLAIN;
            }
            headers.setContentType(mediaType);
            
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (TemplateNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (DataGenerationException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
