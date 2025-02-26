package com.example.tdg.controller;

import com.example.tdg.exception.TemplateNotFoundException;
import com.example.tdg.model.dto.TemplateDto;
import com.example.tdg.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for template management.
 */
@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    
    @Autowired
    private TemplateService templateService;
    
    /**
     * Create a new template.
     * 
     * @param templateDto The template DTO
     * @return The created template
     */
    @PostMapping
    public ResponseEntity<TemplateDto> createTemplate(@Valid @RequestBody TemplateDto templateDto) {
        TemplateDto createdTemplate = templateService.createTemplate(templateDto);
        return new ResponseEntity<>(createdTemplate, HttpStatus.CREATED);
    }
    
    /**
     * Get a template by ID.
     * 
     * @param id The template ID
     * @return The template
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable Long id) {
        try {
            TemplateDto template = templateService.getTemplate(id);
            return ResponseEntity.ok(template);
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all templates with pagination.
     * 
     * @param pageable Pagination information
     * @return Page of templates
     */
    @GetMapping
    public ResponseEntity<Page<TemplateDto>> getAllTemplates(Pageable pageable) {
        Page<TemplateDto> templates = templateService.getAllTemplates(pageable);
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Update a template.
     * 
     * @param id The template ID
     * @param templateDto The updated template
     * @return The updated template
     */
    @PutMapping("/{id}")
    public ResponseEntity<TemplateDto> updateTemplate(@PathVariable Long id, @Valid @RequestBody TemplateDto templateDto) {
        try {
            TemplateDto updatedTemplate = templateService.updateTemplate(id, templateDto);
            return ResponseEntity.ok(updatedTemplate);
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete a template.
     * 
     * @param id The template ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Search templates by name.
     * 
     * @param name The name substring to search for
     * @return List of matching templates
     */
    @GetMapping("/search")
    public ResponseEntity<List<TemplateDto>> searchTemplates(@RequestParam String name) {
        List<TemplateDto> templates = templateService.searchTemplatesByName(name);
        return ResponseEntity.ok(templates);
    }
    
    /**
     * Get available data types and their metadata.
     * 
     * @return Map of data type to metadata
     */
    @GetMapping("/datatypes")
    public ResponseEntity<Map<String, Map<String, String>>> getDataTypes() {
        Map<String, Map<String, String>> dataTypes = templateService.getAvailableDataTypes();
        return ResponseEntity.ok(dataTypes);
    }
}
