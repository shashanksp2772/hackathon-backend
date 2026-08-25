package com.hackathon.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reassignmentEngineOpenApi() {
        return new OpenAPI().info(new Info()
                .title("ZipRun AI Reassignment Engine")
                .description("Reactive reassignment engine: domain model, pluggable routing "
                        + "(rule-based / AI), and the agentic re-planning loop triggered by an "
                        + "agent going OFFLINE.")
                .version("v1"));
    }
}
