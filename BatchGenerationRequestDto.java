package com.example.tdg.model.dto;

import com.example.tdg.model.entity.Template;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Data Transfer Object for batch data generation requests.
 */
public class BatchGenerationRequestDto {
    
    @NotEmpty(message = "Template IDs list cannot be empty")
    private List<Long> templateIds;
    
    @Min(value = 1, message = "Row count must be at least 1")
    private Integer rowCount;
    
    private Template.OutputFormat outputFormat;
    
    private boolean parallel = false;
    
    // Getters and setters
    
    public List<Long> getTemplateIds() {
        return templateIds;
    }
    
    public void setTemplateIds(List<Long> templateIds) {
        this.templateIds = templateIds;
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
    
    public boolean isParallel() {
        return parallel;
    }
    
    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }
}
