package com.example.tdg.service;

import com.example.tdg.exception.TemplateNotFoundException;
import com.example.tdg.model.dto.ColumnDefinitionDto;
import com.example.tdg.model.dto.TemplateDto;
import com.example.tdg.model.entity.ColumnDefinition;
import com.example.tdg.model.entity.Template;
import com.example.tdg.repository.TemplateRepository;
import com.example.tdg.service.generator.DataGenerator;
import com.example.tdg.service.generator.DataTypeRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing templates.
 */
@Service
public class TemplateService {
    
    @Autowired
    private TemplateRepository templateRepository;
    
    @Autowired
    private DataTypeRegistry dataTypeRegistry;
    
    /**
     * Create a new template.
     * 
     * @param templateDto The template DTO
     * @return The created template DTO
     */
    @Transactional
    public TemplateDto createTemplate(TemplateDto templateDto) {
        Template template = mapToEntity(templateDto);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        
        Template savedTemplate = templateRepository.save(template);
        return mapToDto(savedTemplate);
    }
    
    /**
     * Get a template by ID.
     * 
     * @param id The template ID
     * @return The template DTO
     * @throws TemplateNotFoundException If template not found
     */
    public TemplateDto getTemplate(Long id) throws TemplateNotFoundException {
        Template template = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + id));
        
        return mapToDto(template);
    }
    
    /**
     * Get all templates.
     * 
     * @param pageable Pagination information
     * @return Page of template DTOs
     */
    public Page<TemplateDto> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable)
                .map(this::mapToDto);
    }
    
    /**
     * Update a template.
     * 
     * @param id The template ID
     * @param templateDto The updated template DTO
     * @return The updated template DTO
     * @throws TemplateNotFoundException If template not found
     */
    @Transactional
    public TemplateDto updateTemplate(Long id, TemplateDto templateDto) throws TemplateNotFoundException {
        Template existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + id));
        
        // Update fields
        existingTemplate.setName(templateDto.getName());
        existingTemplate.setDescription(templateDto.getDescription());
        existingTemplate.setDefaultOutputFormat(templateDto.getDefaultOutputFormat());
        existingTemplate.setDefaultRowCount(templateDto.getDefaultRowCount());
        existingTemplate.setUpdatedAt(LocalDateTime.now());
        
        // Clear existing column definitions and add new ones
        existingTemplate.getColumnDefinitions().clear();
        
        for (ColumnDefinitionDto columnDto : templateDto.getColumnDefinitions()) {
            ColumnDefinition column = new ColumnDefinition();
            column.setName(columnDto.getName());
            column.setType(columnDto.getType());
            column.setSequenceNumber(columnDto.getSequenceNumber());
            column.setIsNullable(columnDto.getIsNullable());
            column.setNullProbability(columnDto.getNullProbability());
            
            // Convert constraints to string map
            Map<String, String> stringConstraints = new HashMap<>();
            for (Map.Entry<String, Object> entry : columnDto.getConstraints().entrySet()) {
                stringConstraints.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            column.setConstraints(stringConstraints);
            
            existingTemplate.addColumnDefinition(column);
        }
        
        Template updatedTemplate = templateRepository.save(existingTemplate);
        return mapToDto(updatedTemplate);
    }
    
    /**
     * Delete a template.
     * 
     * @param id The template ID
     * @throws TemplateNotFoundException If template not found
     */
    @Transactional
    public void deleteTemplate(Long id) throws TemplateNotFoundException {
        if (!templateRepository.existsById(id)) {
            throw new TemplateNotFoundException("Template not found with ID: " + id);
        }
        
        templateRepository.deleteById(id);
    }
    
    /**
     * Search templates by name.
     * 
     * @param nameSubstring The substring to search for
     * @return List of matching template DTOs
     */
    public List<TemplateDto> searchTemplatesByName(String nameSubstring) {
        return templateRepository.findByNameContainingIgnoreCase(nameSubstring)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get available data types.
     * 
     * @return Map of data type to metadata
     */
    public Map<String, Map<String, String>> getAvailableDataTypes() {
        Map<String, Map<String, String>> result = new HashMap<>();
        
        for (Map.Entry<String, DataGenerator> entry : dataTypeRegistry.getAllGenerators().entrySet()) {
            String type = entry.getKey();
            DataGenerator generator = entry.getValue();
            
            Map<String, String> metadata = new HashMap<>();
            metadata.put("type", type);
            metadata.putAll(generator.getConstraintsMetadata());
            
            result.put(type, metadata);
        }
        
        return result;
    }
    
    /**
     * Map a template entity to a DTO.
     * 
     * @param template The template entity
     * @return The template DTO
     */
    private TemplateDto mapToDto(Template template) {
        TemplateDto dto = new TemplateDto();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setDefaultOutputFormat(template.getDefaultOutputFormat());
        dto.setDefaultRowCount(template.getDefaultRowCount());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        dto.setCreatedBy(template.getCreatedBy());
        
        // Map column definitions
        List<ColumnDefinitionDto> columnDtos = template.getColumnDefinitions().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        
        dto.setColumnDefinitions(columnDtos);
        
        return dto;
    }
    
    /**
     * Map a column definition entity to a DTO.
     * 
     * @param column The column definition entity
     * @return The column definition DTO
     */
    private ColumnDefinitionDto mapToDto(ColumnDefinition column) {
        ColumnDefinitionDto dto = new ColumnDefinitionDto();
        dto.setId(column.getId());
        dto.setName(column.getName());
        dto.setType(column.getType());
        dto.setSequenceNumber(column.getSequenceNumber());
        dto.setIsNullable(column.getIsNullable());
        dto.setNullProbability(column.getNullProbability());
        
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
        dto.setConstraints(typedConstraints);
        
        return dto;
    }
    
    /**
     * Map a template DTO to an entity.
     * 
     * @param dto The template DTO
     * @return The template entity
     */
    private Template mapToEntity(TemplateDto dto) {
        Template template = new Template();
        
        // Don't set ID for new templates
        if (dto.getId() != null) {
            template.setId(dto.getId());
        }
        
        template.setName(dto.getName());
        template.setDescription(dto.getDescription());
        template.setDefaultOutputFormat(dto.getDefaultOutputFormat());
        template.setDefaultRowCount(dto.getDefaultRowCount());
        
        // Handle createdBy and timestamps
        template.setCreatedBy(Optional.ofNullable(dto.getCreatedBy()).orElse("system"));
        template.setCreatedAt(Optional.ofNullable(dto.getCreatedAt()).orElse(LocalDateTime.now()));
        template.setUpdatedAt(Optional.ofNullable(dto.getUpdatedAt()).orElse(LocalDateTime.now()));
        
        // Map column definitions
        for (ColumnDefinitionDto columnDto : dto.getColumnDefinitions()) {
            ColumnDefinition column = new ColumnDefinition();
            
            // Don't set ID for new columns
            if (columnDto.getId() != null) {
                column.setId(columnDto.getId());
            }
            
            column.setName(columnDto.getName());
            column.setType(columnDto.getType());
            column.setSequenceNumber(columnDto.getSequenceNumber());
            column.setIsNullable(columnDto.getIsNullable());
            column.setNullProbability(columnDto.getNullProbability());
            
            // Convert constraints to string map
            Map<String, String> stringConstraints = new HashMap<>();
            for (Map.Entry<String, Object> entry : columnDto.getConstraints().entrySet()) {
                stringConstraints.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            column.setConstraints(stringConstraints);
            
            template.addColumnDefinition(column);
        }
        
        return template;
    }
}
