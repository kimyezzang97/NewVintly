package com.vintly.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // “*“같은 와일드카드를 사용
                .allowedMethods("GET", "POST", "PUT","DELETE")
                .allowedHeaders("*")
                .allowCredentials(true) // 쿠키 인증 요청 허용
                .maxAge(3600);
    }
}
