package com.auth.users_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "permissions")
@lombok.Data
public class PermissionsProperties {



}
