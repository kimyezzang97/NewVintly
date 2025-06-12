package com.vintly.infra.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI vintlyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vintly API")
                        .description("Vintly API")
                        .version("v1.0"));
    }
}
