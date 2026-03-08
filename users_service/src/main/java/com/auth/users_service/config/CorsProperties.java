package com.auth.users_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "cors")
@lombok.Data
public class CorsProperties {
    private List<String> allowedOrigins;
}