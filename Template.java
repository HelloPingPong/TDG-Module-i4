package com.example.tdg.model.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity representing a saved data generation template.
 */
@Entity
@Table(name = "templates")
public class Template {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ColumnDefinition> columnDefinitions = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutputFormat defaultOutputFormat = OutputFormat.CSV;
    
    @Column(nullable = false)
    private Integer defaultRowCount = 100;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(nullable = false)
    private String createdBy = "system";
    
    public enum OutputFormat {
        CSV, JSON, XML
    }
    
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
    
    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }
    
    public void setColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
        this.columnDefinitions.clear();
        if (columnDefinitions != null) {
            this.columnDefinitions.addAll(columnDefinitions);
        }
    }
    
    public void addColumnDefinition(ColumnDefinition columnDefinition) {
        columnDefinitions.add(columnDefinition);
        columnDefinition.setTemplate(this);
    }
    
    public void removeColumnDefinition(ColumnDefinition columnDefinition) {
        columnDefinitions.remove(columnDefinition);
        columnDefinition.setTemplate(null);
    }
    
    public OutputFormat getDefaultOutputFormat() {
        return defaultOutputFormat;
    }
    
    public void setDefaultOutputFormat(OutputFormat defaultOutputFormat) {
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
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Template template = (Template) o;
        return Objects.equals(id, template.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Template{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", columnDefinitions=" + columnDefinitions.size() +
                '}';
    }
}
