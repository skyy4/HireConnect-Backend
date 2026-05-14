package com.hireconnect.profile.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Resolve working-directory-relative 'uploads/' folder as an absolute URI.
        // The trailing slash on addResourceLocations is required by Spring MVC.
        String uploadsDir = Paths.get("uploads").toAbsolutePath().normalize().toUri().toString();
        if (!uploadsDir.endsWith("/")) {
            uploadsDir = uploadsDir + "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsDir);
    }
}
