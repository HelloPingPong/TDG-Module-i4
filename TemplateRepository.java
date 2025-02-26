package com.example.tdg.repository;

import com.example.tdg.model.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Template entities.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    
    /**
     * Find a template by name.
     * 
     * @param name The template name
     * @return Optional containing the template if found
     */
    Optional<Template> findByName(String name);
    
    /**
     * Find templates created by a specific user.
     * 
     * @param createdBy The username
     * @return List of templates
     */
    List<Template> findByCreatedBy(String createdBy);
    
    /**
     * Find templates by name containing a substring.
     * 
     * @param nameSubstring The substring to search for
     * @return List of matching templates
     */
    List<Template> findByNameContainingIgnoreCase(String nameSubstring);
    
    /**
     * Find templates that have a specific column type.
     * 
     * @param columnType The column type to search for
     * @return List of templates
     */
    @Query("SELECT DISTINCT t FROM Template t JOIN t.columnDefinitions c WHERE c.type = :columnType")
    List<Template> findByColumnType(String columnType);
}
