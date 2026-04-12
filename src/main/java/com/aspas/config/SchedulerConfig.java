package com.aspas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Two threads:
        //   Thread 1: End-of-day order generation
        //   Thread 2: Available for future scheduled tasks
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("aspas-scheduler-");
        scheduler.setErrorHandler(throwable -> {
            System.err.println("[SCHEDULER ERROR] " + throwable.getMessage());
            throwable.printStackTrace();
        });
        scheduler.initialize();

        registrar.setTaskScheduler(scheduler);
    }
}