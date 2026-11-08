package com.auth.users_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Signing in from the Android app (https://localhost): its origin is allowed with the configured ones, no other. */
class CorsOriginsTest {

    @RestController
    static class Auth {
        @PostMapping("/api/auth/login")
        String login() {
            return "{}";
        }
    }

    private static CorsProperties properties() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of("https://erastcg.com", "https://www.erastcg.com"));
        return properties;
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new Auth())
            .addFilters(new CorsFilter(new SecurityConfig(null, properties()).corsConfigurationSource()))
            .build();

    private void preflight(String origin, int expected) throws Exception {
        mvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
                .andExpect(status().is(expected));
    }

    @Test
    void theAppIsAllowedByDefault() throws Exception {
        assertThat(properties().origins()).containsExactly("https://erastcg.com", "https://www.erastcg.com", "https://localhost");
        preflight("https://localhost", 200);
        mvc.perform(post("/api/auth/login").header(HttpHeaders.ORIGIN, "https://localhost"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://localhost"));
    }

    @Test
    void theSitesOriginsStillAre() throws Exception {
        preflight("https://www.erastcg.com", 200);
        preflight("https://erastcg.com", 200);
    }

    @Test
    void anyOtherOriginIsRefused() throws Exception {
        preflight("https://evil.example", 403);
        // http://localhost would be MockMvc's own origin (not a CORS request): another port is not
        preflight("http://localhost:4200", 403);
        preflight("https://localhost:8443", 403);
    }

    @Test
    void blanksAndRepeatsAreDropped() {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(List.of(" https://localhost ", ""));
        assertThat(properties.origins()).containsExactly("https://localhost");
        properties.setAllowedOrigins(null);
        properties.setAppOrigins(List.of());
        assertThat(properties.origins()).isEmpty();
    }
}
