package com.example.payment.config;

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
    public OpenAPI paymentServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .description("Simulated payment gateway. Exposes a failure toggle endpoint "
                                + "used to demo the Order Service's Circuit Breaker tripping open.")
                        .version("v1.0.0")
                        .contact(new Contact().name("Jeev").email("dev@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Direct"),
                        new Server().url("http://localhost:8080").description("Via API Gateway")
                ));
    }
}
