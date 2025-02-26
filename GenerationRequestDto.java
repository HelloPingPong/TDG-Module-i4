package com.example.tdg.model.dto;

import com.example.tdg.model.entity.Template;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Data Transfer Object for requesting data generation.
 */
public class GenerationRequestDto {
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
    
    @Min(value = 1, message = "Row count must be at least 1")
    private Integer rowCount;
    
    private Template.OutputFormat outputFormat;
    
    private String filename;
    
    // Getters and setters
    
    public Long getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
    
    public Integer getRowCount() {
        return rowCount;
    }
    
    public void setRowCount(Integer rowCount) {
        this.rowCount = rowCount;
    }
    
    public Template.OutputFormat getOutputFormat() {
        return outputFormat;
    }
    
    public void setOutputFormat(Template.OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
}
