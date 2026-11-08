package com.auth.users_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@ConfigurationProperties(prefix = "cors")
@lombok.Data
public class CorsProperties {
    private List<String> allowedOrigins;

    /** The Android app's WebView (Capacitor serves the page at https://localhost): always allowed. */
    private List<String> appOrigins = new ArrayList<>(List.of("https://localhost"));

    /** The configured origins plus the app's, without blanks or repeats. */
    public List<String> origins() {
        return Stream.concat(
                        allowedOrigins == null ? Stream.empty() : allowedOrigins.stream(),
                        appOrigins == null ? Stream.empty() : appOrigins.stream())
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .distinct()
                .toList();
    }
}