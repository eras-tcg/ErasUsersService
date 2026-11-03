package com.auth.users_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "user")
@lombok.Data
public class UserProperties {

    private Username username = new Username();


    private boolean canDeleteOwnAccount;

    @lombok.Data
    public static class Username {
        private int minLength;
        private int maxLength;
    }

}