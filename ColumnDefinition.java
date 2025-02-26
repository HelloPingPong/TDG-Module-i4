package com.example.tdg.model.entity;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Entity representing a column definition within a template.
 */
@Entity
@Table(name = "column_definitions")
public class ColumnDefinition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String type;
    
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "column_constraints",
        joinColumns = @JoinColumn(name = "column_definition_id")
    )
    @MapKeyColumn(name = "constraint_name")
    @Column(name = "constraint_value")
    private Map<String, String> constraints = new HashMap<>();
    
    @Column(name = "is_nullable", nullable = false)
    private Boolean isNullable = false;
    
    @Column(name = "null_probability")
    private Double nullProbability = 0.0;
    
    // Getters and setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Template getTemplate() {
        return template;
    }
    
    public void setTemplate(Template template) {
        this.template = template;
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
    
    public Map<String, String> getConstraints() {
        return constraints;
    }
    
    public void setConstraints(Map<String, String> constraints) {
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnDefinition that = (ColumnDefinition) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "ColumnDefinition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
