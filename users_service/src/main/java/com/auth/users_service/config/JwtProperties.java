package com.auth.users_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "jwt")
@lombok.Data
public class JwtProperties {
    private String secret;
    private long expiration;
}