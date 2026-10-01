package com.github.chaunguyentruongan.warehouse_cdnsg.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class CorsConfig {

    @Value("${app.cors.origin}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigPath() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
             registry.addMapping("/**")
                .allowedOriginPatterns(
                    "https://*.vercel.app",
                    "http://localhost:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
                    }
                };
    }
}
