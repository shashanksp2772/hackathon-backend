package com.hackathon.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * A small, dedicated pool for the agentic re-planning loop — deliberately
 * not Spring's default {@code SimpleAsyncTaskExecutor}, which creates an
 * unbounded thread per task.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "reassignmentExecutor")
    public TaskExecutor reassignmentExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("reassignment-");
        executor.initialize();
        return executor;
    }
}
