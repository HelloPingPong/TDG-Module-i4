package com.example.tdg.service;

import com.example.tdg.exception.ScheduleNotFoundException;
import com.example.tdg.exception.TemplateNotFoundException;
import com.example.tdg.model.dto.GenerationScheduleDto;
import com.example.tdg.model.entity.GenerationSchedule;
import com.example.tdg.model.entity.Template;
import com.example.tdg.repository.GenerationScheduleRepository;
import com.example.tdg.repository.TemplateRepository;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing generation schedules.
 */
@Service
public class ScheduleService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);
    
    @Autowired
    private GenerationScheduleRepository scheduleRepository;
    
    @Autowired
    private TemplateRepository templateRepository;
    
    /**
     * Create a new generation schedule.
     * 
     * @param scheduleDto The schedule DTO
     * @return The created schedule DTO
     * @throws TemplateNotFoundException If template not found
     * @throws IllegalArgumentException If cron expression is invalid
     */
    @Transactional
    public GenerationScheduleDto createSchedule(GenerationScheduleDto scheduleDto) 
            throws TemplateNotFoundException, IllegalArgumentException {
        
        Template template = templateRepository.findById(scheduleDto.getTemplateId())
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + scheduleDto.getTemplateId()));
        
        GenerationSchedule schedule = mapToEntity(scheduleDto, template);
        
        // Validate and set next run time
        validateAndSetNextRunTime(schedule);
        
        GenerationSchedule savedSchedule = scheduleRepository.save(schedule);
        return mapToDto(savedSchedule);
    }
    
    /**
     * Get a schedule by ID.
     * 
     * @param id The schedule ID
     * @return The schedule DTO
     * @throws ScheduleNotFoundException If schedule not found
     */
    public GenerationScheduleDto getSchedule(Long id) throws ScheduleNotFoundException {
        GenerationSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found with ID: " + id));
        
        return mapToDto(schedule);
    }
    
    /**
     * Get all schedules.
     * 
     * @param pageable Pagination information
     * @return Page of schedule DTOs
     */
    public Page<GenerationScheduleDto> getAllSchedules(Pageable pageable) {
        return scheduleRepository.findAll(pageable)
                .map(this::mapToDto);
    }
    
    /**
     * Update a schedule.
     * 
     * @param id The schedule ID
     * @param scheduleDto The updated schedule DTO
     * @return The updated schedule DTO
     * @throws ScheduleNotFoundException If schedule not found
     * @throws TemplateNotFoundException If template not found
     * @throws IllegalArgumentException If cron expression is invalid
     */
    @Transactional
    public GenerationScheduleDto updateSchedule(Long id, GenerationScheduleDto scheduleDto) 
            throws ScheduleNotFoundException, TemplateNotFoundException, IllegalArgumentException {
        
        GenerationSchedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found with ID: " + id));
        
        Template template = templateRepository.findById(scheduleDto.getTemplateId())
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + scheduleDto.getTemplateId()));
        
        // Update fields
        existingSchedule.setTemplate(template);
        existingSchedule.setName(scheduleDto.getName());
        existingSchedule.setDescription(scheduleDto.getDescription());
        existingSchedule.setRowCount(scheduleDto.getRowCount());
        existingSchedule.setOutputFormat(scheduleDto.getOutputFormat());
        existingSchedule.setStatus(scheduleDto.getStatus() != null ? scheduleDto.getStatus() : existingSchedule.getStatus());
        existingSchedule.setUpdatedAt(LocalDateTime.now());
        
        // Update cron expression or next run time
        existingSchedule.setCronExpression(scheduleDto.getCronExpression());
        existingSchedule.setNextRunTime(scheduleDto.getNextRunTime());
        
        // Validate and set next run time
        validateAndSetNextRunTime(existingSchedule);
        
        GenerationSchedule updatedSchedule = scheduleRepository.save(existingSchedule);
        return mapToDto(updatedSchedule);
    }
    
    /**
     * Delete a schedule.
     * 
     * @param id The schedule ID
     * @throws ScheduleNotFoundException If schedule not found
     */
    @Transactional
    public void deleteSchedule(Long id) throws ScheduleNotFoundException {
        if (!scheduleRepository.existsById(id)) {
            throw new ScheduleNotFoundException("Schedule not found with ID: " + id);
        }
        
        scheduleRepository.deleteById(id);
    }
    
    /**
     * Get schedules for a template.
     * 
     * @param templateId The template ID
     * @return List of schedule DTOs
     */
    public List<GenerationScheduleDto> getSchedulesForTemplate(Long templateId) {
        return scheduleRepository.findByTemplateId(templateId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get schedules by status.
     * 
     * @param status The status
     * @return List of schedule DTOs
     */
    public List<GenerationScheduleDto> getSchedulesByStatus(GenerationSchedule.Status status) {
        return scheduleRepository.findByStatus(status)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Find schedules that are due to run.
     * 
     * @return List of schedule IDs
     */
    public List<Long> findSchedulesDueToRun() {
        return scheduleRepository.findSchedulesDueToRun(LocalDateTime.now())
                .stream()
                .map(GenerationSchedule::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * Update a schedule's next run time based on its cron expression.
     * 
     * @param scheduleId The schedule ID
     * @throws ScheduleNotFoundException If schedule not found
     */
    @Transactional
    public void updateNextRunTime(Long scheduleId) throws ScheduleNotFoundException {
        GenerationSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found with ID: " + scheduleId));
        
        String cronExpression = schedule.getCronExpression();
        if (cronExpression != null && !cronExpression.isEmpty()) {
            try {
                CronExpression cron = new CronExpression(cronExpression);
                Date now = new Date();
                Date nextRunDate = cron.getNextValidTimeAfter(now);
                
                if (nextRunDate != null) {
                    LocalDateTime nextRunTime = LocalDateTime.ofInstant(
                            nextRunDate.toInstant(), ZoneId.systemDefault());
                    schedule.setNextRunTime(nextRunTime);
                    scheduleRepository.save(schedule);
                }
            } catch (ParseException e) {
                logger.error("Invalid cron expression: {}", cronExpression, e);
            }
        }
    }
    
    /**
     * Update a schedule's last run information.
     * 
     * @param scheduleId The schedule ID
     * @param result The result of the run
     * @throws ScheduleNotFoundException If schedule not found
     */
    @Transactional
    public void updateLastRunInfo(Long scheduleId, String result) throws ScheduleNotFoundException {
        GenerationSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found with ID: " + scheduleId));
        
        schedule.setLastRunTime(LocalDateTime.now());
        schedule.setLastRunResult(result);
        
        scheduleRepository.save(schedule);
    }
    
    /**
     * Validate and set the next run time for a schedule.
     * 
     * @param schedule The schedule
     * @throws IllegalArgumentException If cron expression is invalid
     */
    private void validateAndSetNextRunTime(GenerationSchedule schedule) throws IllegalArgumentException {
        String cronExpression = schedule.getCronExpression();
        LocalDateTime nextRunTime = schedule.getNextRunTime();
        
        if (cronExpression != null && !cronExpression.isEmpty()) {
            try {
                CronExpression cron = new CronExpression(cronExpression);
                Date now = new Date();
                Date nextRunDate = cron.getNextValidTimeAfter(now);
                
                if (nextRunDate != null) {
                    schedule.setNextRunTime(LocalDateTime.ofInstant(
                            nextRunDate.toInstant(), ZoneId.systemDefault()));
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid cron expression: " + cronExpression, e);
            }
        } else if (nextRunTime == null) {
            throw new IllegalArgumentException("Either cron expression or next run time must be provided");
        }
        
        // Set status to ACTIVE if not already set
        if (schedule.getStatus() == null) {
            schedule.setStatus(GenerationSchedule.Status.ACTIVE);
        }
    }
    
    /**
     * Map a schedule entity to a DTO.
     * 
     * @param schedule The schedule entity
     * @return The schedule DTO
     */
    private GenerationScheduleDto mapToDto(GenerationSchedule schedule) {
        GenerationScheduleDto dto = new GenerationScheduleDto();
        dto.setId(schedule.getId());
        dto.setTemplateId(schedule.getTemplate().getId());
        dto.setName(schedule.getName());
        dto.setDescription(schedule.getDescription());
        dto.setRowCount(schedule.getRowCount());
        dto.setOutputFormat(schedule.getOutputFormat());
        dto.setNextRunTime(schedule.getNextRunTime());
        dto.setCronExpression(schedule.getCronExpression());
        dto.setStatus(schedule.getStatus());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setUpdatedAt(schedule.getUpdatedAt());
        dto.setCreatedBy(schedule.getCreatedBy());
        dto.setLastRunTime(schedule.getLastRunTime());
        dto.setLastRunResult(schedule.getLastRunResult());
        
        return dto;
    }
    
    /**
     * Map a schedule DTO to an entity.
     * 
     * @param dto The schedule DTO
     * @param template The template
     * @return The schedule entity
     */
    private GenerationSchedule mapToEntity(GenerationScheduleDto dto, Template template) {
        GenerationSchedule schedule = new GenerationSchedule();
        
        // Don't set ID for new schedules
        if (dto.getId() != null) {
            schedule.setId(dto.getId());
        }
        
        schedule.setTemplate(template);
        schedule.setName(dto.getName());
        schedule.setDescription(dto.getDescription());
        schedule.setRowCount(dto.getRowCount());
        schedule.setOutputFormat(dto.getOutputFormat());
        schedule.setNextRunTime(dto.getNextRunTime());
        schedule.setCronExpression(dto.getCronExpression());
        schedule.setStatus(dto.getStatus() != null ? dto.getStatus() : GenerationSchedule.Status.CREATED);
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());
        schedule.setCreatedBy(dto.getCreatedBy() != null ? dto.getCreatedBy() : "system");
        
        return schedule;
    }
}
