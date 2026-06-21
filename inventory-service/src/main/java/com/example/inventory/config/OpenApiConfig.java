package com.example.inventory.config;

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
    public OpenAPI inventoryServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory Service API")
                        .description("Saga participant: reserves and releases stock. "
                                + "Release is invoked by the Order Service as a compensating "
                                + "transaction when a later Saga step fails.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Jeev").email("dev@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("Direct"),
                        new Server().url("http://localhost:8080").description("Via API Gateway")
                ));
    }
}
