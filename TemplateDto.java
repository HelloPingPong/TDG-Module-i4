package com.example.tdg.model.dto;

import com.example.tdg.model.entity.Template;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Template entity.
 */
public class TemplateDto {
    
    private Long id;
    
    @NotBlank(message = "Template name is required")
    private String name;
    
    private String description;
    
    @NotEmpty(message = "At least one column definition is required")
    @Valid
    private List<ColumnDefinitionDto> columnDefinitions = new ArrayList<>();
    
    @NotNull(message = "Default output format is required")
    private Template.OutputFormat defaultOutputFormat;
    
    @NotNull(message = "Default row count is required")
    @Min(value = 1, message = "Default row count must be at least 1")
    private Integer defaultRowCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<ColumnDefinitionDto> getColumnDefinitions() {
        return columnDefinitions;
    }
    
    public void setColumnDefinitions(List<ColumnDefinitionDto> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }
    
    public Template.OutputFormat getDefaultOutputFormat() {
        return defaultOutputFormat;
    }
    
    public void setDefaultOutputFormat(Template.OutputFormat defaultOutputFormat) {
        this.defaultOutputFormat = defaultOutputFormat;
    }
    
    public Integer getDefaultRowCount() {
        return defaultRowCount;
    }
    
    public void setDefaultRowCount(Integer defaultRowCount) {
        this.defaultRowCount = defaultRowCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
