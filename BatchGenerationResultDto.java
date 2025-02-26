package com.example.tdg.model.dto;

import com.example.tdg.model.entity.Template;

/**
 * Data Transfer Object for batch generation results.
 */
public class BatchGenerationResultDto {
    
    private Long templateId;
    
    private boolean success;
    
    private String message;
    
    private long durationMillis;
    
    private Template.OutputFormat outputFormat;
    
    private int dataSize;
    
    private String dataPreview;
    
    // Getters and setters
    
    public Long getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getDurationMillis() {
        return durationMillis;
    }
    
    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }
    
    public Template.OutputFormat getOutputFormat() {
        return outputFormat;
    }
    
    public void setOutputFormat(Template.OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    public int getDataSize() {
        return dataSize;
    }
    
    public void setDataSize(int dataSize) {
        this.dataSize = dataSize;
    }
    
    public String getDataPreview() {
        return dataPreview;
    }
    
    public void setDataPreview(String dataPreview) {
        this.dataPreview = dataPreview;
    }
}
