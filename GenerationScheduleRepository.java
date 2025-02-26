package com.example.tdg.repository;

import com.example.tdg.model.entity.GenerationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for GenerationSchedule entities.
 */
@Repository
public interface GenerationScheduleRepository extends JpaRepository<GenerationSchedule, Long> {
    
    /**
     * Find schedules for a specific template.
     * 
     * @param templateId The template ID
     * @return List of schedules
     */
    List<GenerationSchedule> findByTemplateId(Long templateId);
    
    /**
     * Find active schedules with next run time before or equal to a given time.
     * 
     * @param time The time to compare against
     * @return List of schedules that are due to run
     */
    @Query("SELECT s FROM GenerationSchedule s WHERE s.status = 'ACTIVE' AND s.nextRunTime <= :time")
    List<GenerationSchedule> findSchedulesDueToRun(LocalDateTime time);
    
    /**
     * Find schedules by status.
     * 
     * @param status The status to filter by
     * @return List of schedules
     */
    List<GenerationSchedule> findByStatus(GenerationSchedule.Status status);
    
    /**
     * Find schedules created by a specific user.
     * 
     * @param createdBy The username
     * @return List of schedules
     */
    List<GenerationSchedule> findByCreatedBy(String createdBy);
}
