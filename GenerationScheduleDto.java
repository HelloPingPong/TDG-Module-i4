package com.example.tdg.model.dto;

import com.example.tdg.model.entity.GenerationSchedule;
import com.example.tdg.model.entity.Template;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for GenerationSchedule entity.
 */
public class GenerationScheduleDto {
    
    private Long id;
    
    @NotNull(message = "Template ID is required")
    private Long templateId;
    
    @NotBlank(message = "Schedule name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Row count is required")
    @Min(value = 1, message = "Row count must be at least 1")
    private Integer rowCount;
    
    @NotNull(message = "Output format is required")
    private Template.OutputFormat outputFormat;
    
    private LocalDateTime nextRunTime;
    
    private String cronExpression;
    
    private GenerationSchedule.Status status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private LocalDateTime lastRunTime;
    
    private String lastRunResult;
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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
    
    public LocalDateTime getNextRunTime() {
        return nextRunTime;
    }
    
    public void setNextRunTime(LocalDateTime nextRunTime) {
        this.nextRunTime = nextRunTime;
    }
    
    public String getCronExpression() {
        return cronExpression;
    }
    
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    public GenerationSchedule.Status getStatus() {
        return status;
    }
    
    public void setStatus(GenerationSchedule.Status status) {
        this.status = status;
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
    
    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }
    
    public void setLastRunTime(LocalDateTime lastRunTime) {
        this.lastRunTime = lastRunTime;
    }
    
    public String getLastRunResult() {
        return lastRunResult;
    }
    
    public void setLastRunResult(String lastRunResult) {
        this.lastRunResult = lastRunResult;
    }
}
