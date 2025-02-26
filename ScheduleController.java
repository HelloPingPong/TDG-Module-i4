package com.example.tdg.controller;

import com.example.tdg.exception.ScheduleNotFoundException;
import com.example.tdg.exception.TemplateNotFoundException;
import com.example.tdg.model.dto.GenerationScheduleDto;
import com.example.tdg.model.entity.GenerationSchedule;
import com.example.tdg.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST API controller for schedule management.
 */
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    /**
     * Create a new generation schedule.
     * 
     * @param scheduleDto The schedule DTO
     * @return The created schedule
     */
    @PostMapping
    public ResponseEntity<?> createSchedule(@Valid @RequestBody GenerationScheduleDto scheduleDto) {
        try {
            GenerationScheduleDto createdSchedule = scheduleService.createSchedule(scheduleDto);
            return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * Get a schedule by ID.
     * 
     * @param id The schedule ID
     * @return The schedule
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenerationScheduleDto> getSchedule(@PathVariable Long id) {
        try {
            GenerationScheduleDto schedule = scheduleService.getSchedule(id);
            return ResponseEntity.ok(schedule);
        } catch (ScheduleNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all schedules with pagination.
     * 
     * @param pageable Pagination information
     * @return Page of schedules
     */
    @GetMapping
    public ResponseEntity<Page<GenerationScheduleDto>> getAllSchedules(Pageable pageable) {
        Page<GenerationScheduleDto> schedules = scheduleService.getAllSchedules(pageable);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * Update a schedule.
     * 
     * @param id The schedule ID
     * @param scheduleDto The updated schedule
     * @return The updated schedule
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @Valid @RequestBody GenerationScheduleDto scheduleDto) {
        try {
            GenerationScheduleDto updatedSchedule = scheduleService.updateSchedule(id, scheduleDto);
            return ResponseEntity.ok(updatedSchedule);
        } catch (ScheduleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * Delete a schedule.
     * 
     * @param id The schedule ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return ResponseEntity.noContent().build();
        } catch (ScheduleNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get schedules for a template.
     * 
     * @param templateId The template ID
     * @return List of schedules
     */
    @GetMapping("/byTemplate/{templateId}")
    public ResponseEntity<List<GenerationScheduleDto>> getSchedulesForTemplate(@PathVariable Long templateId) {
        List<GenerationScheduleDto> schedules = scheduleService.getSchedulesForTemplate(templateId);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * Get schedules by status.
     * 
     * @param status The status
     * @return List of schedules
     */
    @GetMapping("/byStatus/{status}")
    public ResponseEntity<List<GenerationScheduleDto>> getSchedulesByStatus(@PathVariable String status) {
        try {
            GenerationSchedule.Status scheduleStatus = GenerationSchedule.Status.valueOf(status.toUpperCase());
            List<GenerationScheduleDto> schedules = scheduleService.getSchedulesByStatus(scheduleStatus);
            return ResponseEntity.ok(schedules);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Activate a schedule.
     * 
     * @param id The schedule ID
     * @return The updated schedule
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateSchedule(@PathVariable Long id) {
        try {
            GenerationScheduleDto schedule = scheduleService.getSchedule(id);
            schedule.setStatus(GenerationSchedule.Status.ACTIVE);
            GenerationScheduleDto updatedSchedule = scheduleService.updateSchedule(id, schedule);
            return ResponseEntity.ok(updatedSchedule);
        } catch (ScheduleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * Pause a schedule.
     * 
     * @param id The schedule ID
     * @return The updated schedule
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<?> pauseSchedule(@PathVariable Long id) {
        try {
            GenerationScheduleDto schedule = scheduleService.getSchedule(id);
            schedule.setStatus(GenerationSchedule.Status.PAUSED);
            GenerationScheduleDto updatedSchedule = scheduleService.updateSchedule(id, schedule);
            return ResponseEntity.ok(updatedSchedule);
        } catch (ScheduleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (TemplateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
