package com.jobsearch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Search API - Vulnerable Training App")
                        .version("1.0.0")
                        .description("⚠️ INTENTIONALLY VULNERABLE API for security training purposes. Contains OWASP Top 10 vulnerabilities.")
                        .contact(new Contact()
                                .name("Security Training")
                                .email("security@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Local Development Server"),
                        new Server().url("http://localhost:8083").description("Docker Container")
                ));
    }
}