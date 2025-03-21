package com.vintly.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") //  “*“같은 와일드카드를 사용
                //.allowedOrigins("http://localhost:8080", "http://182.225.186.228")
                .allowedMethods("GET", "POST", "PUT","DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true) // 쿠키 인증 요청 허용
                .maxAge(3600);
    }
}
