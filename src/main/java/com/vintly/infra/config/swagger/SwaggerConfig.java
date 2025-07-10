package com.vintly.infra.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                        .version("v1.0"))
                .addSecurityItem(new SecurityRequirement().addList("access")) // 헤더 이름
                .components(new Components()
                        .addSecuritySchemes("access",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("access") // 헤더 이름을 'access'로 명시
                        ));
    }
}
