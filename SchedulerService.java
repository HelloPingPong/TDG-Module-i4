package com.example.tdg.service.scheduler;

import com.example.tdg.exception.ScheduleNotFoundException;
import com.example.tdg.model.dto.GenerationScheduleDto;
import com.example.tdg.model.entity.GenerationSchedule;
import com.example.tdg.service.ScheduleService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service for managing Quartz scheduler jobs for data generation.
 */
@Service
public class SchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    
    @Autowired
    private Scheduler scheduler;
    
    @Autowired
    private ScheduleService scheduleService;
    
    /**
     * Initialize scheduler on application startup.
     */
    @PostConstruct
    public void init() {
        try {
            // Clear any existing jobs
            scheduler.clear();
            
            // Restore active schedules
            List<GenerationScheduleDto> activeSchedules = scheduleService.getSchedulesByStatus(GenerationSchedule.Status.ACTIVE);
            
            logger.info("Initializing scheduler with {} active schedules", activeSchedules.size());
            
            for (GenerationScheduleDto schedule : activeSchedules) {
                scheduleJob(schedule);
            }
        } catch (Exception e) {
            logger.error("Error initializing scheduler: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Schedule a job for a generation schedule.
     * 
     * @param schedule The schedule DTO
     * @throws SchedulerException If scheduling fails
     */
    public void scheduleJob(GenerationScheduleDto schedule) throws SchedulerException {
        String jobId = "job_" + schedule.getId();
        String triggerId = "trigger_" + schedule.getId();
        
        // Create job
        JobDetail job = JobBuilder.newJob(GenerationJob.class)
                .withIdentity(jobId)
                .usingJobData("scheduleId", schedule.getId())
                .usingJobData("outputFormat", schedule.getOutputFormat().toString())
                .storeDurably()
                .build();
        
        // Create trigger
        Trigger trigger;
        
        if (schedule.getCronExpression() != null && !schedule.getCronExpression().isEmpty()) {
            // For cron-based schedules
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerId)
                    .forJob(job)
                    .withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCronExpression()))
                    .build();
            
            logger.info("Scheduling job with cron expression: {}", schedule.getCronExpression());
        } else if (schedule.getNextRunTime() != null) {
            // For one-time schedules
            Date startTime = Date.from(schedule.getNextRunTime().atZone(ZoneId.systemDefault()).toInstant());
            
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerId)
                    .forJob(job)
                    .startAt(startTime)
                    .build();
            
            logger.info("Scheduling one-time job at: {}", schedule.getNextRunTime());
        } else {
            throw new SchedulerException("Schedule must have either cron expression or next run time");
        }
        
        // Schedule the job
        scheduler.scheduleJob(job, trigger);
    }
    
    /**
     * Unschedule a job for a generation schedule.
     * 
     * @param scheduleId The schedule ID
     * @throws SchedulerException If unscheduling fails
     */
    public void unscheduleJob(Long scheduleId) throws SchedulerException {
        String jobId = "job_" + scheduleId;
        String triggerId = "trigger_" + scheduleId;
        
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerId));
        scheduler.deleteJob(JobKey.jobKey(jobId));
        
        logger.info("Unscheduled job for schedule ID: {}", scheduleId);
    }
    
    /**
     * Update schedule status and job scheduling.
     * 
     * @param scheduleId The schedule ID
     * @param active Whether the schedule should be active
     * @throws ScheduleNotFoundException If schedule not found
     * @throws SchedulerException If scheduler operation fails
     */
    public void updateScheduleStatus(Long scheduleId, boolean active) 
            throws ScheduleNotFoundException, SchedulerException {
        
        GenerationScheduleDto schedule = scheduleService.getSchedule(scheduleId);
        
        if (active) {
            schedule.setStatus(GenerationSchedule.Status.ACTIVE);
            scheduleService.updateSchedule(scheduleId, schedule);
            scheduleJob(schedule);
        } else {
            schedule.setStatus(GenerationSchedule.Status.PAUSED);
            scheduleService.updateSchedule(scheduleId, schedule);
            unscheduleJob(scheduleId);
        }
    }
    
    /**
     * Execute job immediately, regardless of schedule.
     * 
     * @param scheduleId The schedule ID
     * @throws ScheduleNotFoundException If schedule not found
     * @throws SchedulerException If job execution fails
     */
    public void executeJobNow(Long scheduleId) throws ScheduleNotFoundException, SchedulerException {
        GenerationScheduleDto schedule = scheduleService.getSchedule(scheduleId);
        
        String jobId = "job_" + scheduleId;
        
        // Create job data map
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("scheduleId", scheduleId);
        dataMap.put("outputFormat", schedule.getOutputFormat().toString());
        
        // Execute job
        scheduler.triggerJob(JobKey.jobKey(jobId), dataMap);
        
        logger.info("Triggered immediate execution of job for schedule ID: {}", scheduleId);
    }
    
    /**
     * Periodically check for schedules that need to be run.
     * This handles schedules with nextRunTime but no cron expression.
     */
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkSchedules() {
        logger.debug("Checking for schedules due to run");
        
        try {
            List<Long> dueSchedules = scheduleService.findSchedulesDueToRun();
            
            for (Long scheduleId : dueSchedules) {
                try {
                    logger.info("Executing schedule that is due: {}", scheduleId);
                    executeJobNow(scheduleId);
                    
                    // For non-recurring schedules, mark as completed after execution
                    GenerationScheduleDto schedule = scheduleService.getSchedule(scheduleId);
                    if (schedule.getCronExpression() == null || schedule.getCronExpression().isEmpty()) {
                        schedule.setStatus(GenerationSchedule.Status.COMPLETED);
                        scheduleService.updateSchedule(scheduleId, schedule);
                    }
                } catch (Exception e) {
                    logger.error("Error executing due schedule {}: {}", scheduleId, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error checking for due schedules: {}", e.getMessage(), e);
        }
    }
}
