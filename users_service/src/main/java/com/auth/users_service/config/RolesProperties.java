package com.auth.users_service.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "roles")
@lombok.Data
public class RolesProperties {

    private Boolean allowed;
    private String baseRole;

    private String baseUsername;
    private String basePassword;
}
