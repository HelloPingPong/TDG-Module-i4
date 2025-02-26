package com.example.tdg.service.scheduler;

import com.example.tdg.exception.DataGenerationException;
import com.example.tdg.exception.ScheduleNotFoundException;
import com.example.tdg.model.entity.GenerationSchedule;
import com.example.tdg.service.DataGenerationService;
import com.example.tdg.service.ScheduleService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Quartz job for scheduled data generation.
 */
@Component
public class GenerationJob implements Job {
    
    private static final Logger logger = LoggerFactory.getLogger(GenerationJob.class);
    
    @Autowired
    private DataGenerationService dataGenerationService;
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Long scheduleId = dataMap.getLong("scheduleId");
        
        logger.info("Executing scheduled generation job for schedule ID: {}", scheduleId);
        
        try {
            // Get schedule details
            GenerationSchedule.Status originalStatus = null;
            Long templateId = null;
            int rowCount = 0;
            GenerationSchedule.Status scheduleStatus = null;
            
            try {
                GenerationSchedule schedule = scheduleService.getScheduleEntity(scheduleId);
                originalStatus = schedule.getStatus();
                templateId = schedule.getTemplate().getId();
                rowCount = schedule.getRowCount();
                scheduleStatus = schedule.getStatus();
            } catch (ScheduleNotFoundException e) {
                logger.error("Schedule not found for ID: {}", scheduleId);
                return;
            }
            
            // Check if schedule is active
            if (scheduleStatus != GenerationSchedule.Status.ACTIVE) {
                logger.info("Skipping job execution because schedule is not active. Status: {}", scheduleStatus);
                return;
            }
            
            // Generate data
            byte[] data = dataGenerationService.generateData(
                    templateId,
                    rowCount,
                    dataMap.getString("outputFormat")
            );
            
            // Save data to file
            String filename = saveGeneratedData(data, scheduleId, dataMap.getString("outputFormat"));
            
            // Update schedule with success result
            scheduleService.updateLastRunInfo(scheduleId, "Generated " + rowCount + " rows, saved to " + filename);
            
            // Update next run time for recurring schedules
            scheduleService.updateNextRunTime(scheduleId);
            
            logger.info("Scheduled generation completed successfully for schedule ID: {}", scheduleId);
        } catch (Exception e) {
            logger.error("Error executing scheduled generation job: {}", e.getMessage(), e);
            
            try {
                // Update schedule with error result
                scheduleService.updateLastRunInfo(scheduleId, "Error: " + e.getMessage());
            } catch (ScheduleNotFoundException ex) {
                logger.error("Could not update schedule with error info", ex);
            }
            
            throw new JobExecutionException(e);
        }
    }
    
    /**
     * Save generated data to a file.
     * 
     * @param data The generated data
     * @param scheduleId The schedule ID
     * @param outputFormat The output format
     * @return The filename
     * @throws IOException If file saving fails
     */
    private String saveGeneratedData(byte[] data, Long scheduleId, String outputFormat) throws IOException {
        // Ensure output directory exists
        Path outputDir = Paths.get("generated-data");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        // Create filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = outputFormat.toLowerCase();
        String filename = String.format("schedule_%d_%s.%s", scheduleId, timestamp, extension);
        
        File outputFile = new File(outputDir.toFile(), filename);
        
        // Write data to file
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(data);
        }
        
        return outputFile.getAbsolutePath();
    }
}
