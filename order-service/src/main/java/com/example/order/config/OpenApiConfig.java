package com.example.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Saga Orchestrator: coordinates Inventory reservation and Payment "
                                + "charge, and runs compensating transactions on failure. "
                                + "The Payment call is protected by a Circuit Breaker (Resilience4j).")
                        .version("v1.0.0")
                        .contact(new Contact().name("Jeev").email("dev@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Direct"),
                        new Server().url("http://localhost:8080").description("Via API Gateway")
                ));
    }
}
