package com.example.tdg.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for Quartz scheduler.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    
    /**
     * Create and configure Quartz scheduler.
     * 
     * @return Configured Quartz scheduler
     * @throws SchedulerException If scheduler initialization fails
     */
    @Bean
    public Scheduler scheduler() throws SchedulerException {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        return scheduler;
    }
}
