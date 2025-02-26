package com.example.tdg.model.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object for ColumnDefinition entity.
 */
public class ColumnDefinitionDto {
    
    private Long id;
    
    @NotBlank(message = "Column name is required")
    private String name;
    
    @NotBlank(message = "Column type is required")
    private String type;
    
    @NotNull(message = "Sequence number is required")
    private Integer sequenceNumber;
    
    private Map<String, Object> constraints = new HashMap<>();
    
    private Boolean isNullable = false;
    
    private Double nullProbability = 0.0;
    
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public Map<String, Object> getConstraints() {
        return constraints;
    }
    
    public void setConstraints(Map<String, Object> constraints) {
        this.constraints = constraints;
    }
    
    public Boolean getIsNullable() {
        return isNullable;
    }
    
    public void setIsNullable(Boolean nullable) {
        isNullable = nullable;
    }
    
    public Double getNullProbability() {
        return nullProbability;
    }
    
    public void setNullProbability(Double nullProbability) {
        this.nullProbability = nullProbability;
    }
}
