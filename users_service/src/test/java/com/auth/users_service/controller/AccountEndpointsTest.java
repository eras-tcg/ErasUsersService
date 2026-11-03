package com.auth.users_service.controller;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;

import com.auth.users_service.dto.EditUserRequest;
import com.auth.users_service.dto.LoginRequest;
import com.auth.users_service.service.AuthService;
import com.auth.users_service.service.CheckAccessService;
import com.auth.users_service.service.LoginThrottle;
import com.auth.users_service.service.UsersManagerService;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** How the login and user-editing endpoints answer the new refusals. */
class AccountEndpointsTest {

    private final AuthService authService = mock(AuthService.class);
    private final UsersManagerService usersManager = mock(UsersManagerService.class);
    private final AuthController auth = new AuthController(authService, usersManager, mock(CheckAccessService.class));
    private final UsersController usersController = new UsersController(usersManager);

    @Test
    void wrongCredentialsAre401WithTheGenericMessage() {
        when(authService.login(anyString(), anyString(), anyString()))
            .thenThrow(new BadCredentialsException(AuthService.INVALID_CREDENTIALS));

        ResponseEntity<Object> response = login();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Error: " + AuthService.INVALID_CREDENTIALS);
    }

    @Test
    void tooManyAttemptsAre429() {
        when(authService.login(anyString(), anyString(), anyString())).thenThrow(new LoginThrottle.TooManyAttemptsException());

        assertThat(login().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void theClientIpIsPassedToTheLogin() {
        when(authService.login(eq("ana"), eq("pw"), eq("203.0.113.7"))).thenReturn("token");

        assertThat(login().getBody()).isEqualTo(Map.of("token", "token"));
    }

    @Test
    void editingSomeoneElseAnswersWithoutAToken() {
        when(usersManager.updateUser(eq("2"), any())).thenReturn(null);

        ResponseEntity<Object> response = usersController.updateUser("2", new EditUserRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of());
    }

    @Test
    void aRoleChangeWithoutManageRolesIs403() {
        when(usersManager.updateUser(eq("2"), any())).thenThrow(new AccessDeniedException("no"));

        assertThat(usersController.updateUser("2", new EditUserRequest()).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void aResetBreakingThePolicyIs400() {
        org.mockito.Mockito.doThrow(new IllegalArgumentException("Password must be at least 8 characters"))
            .when(usersManager).setPassword("2", "short");

        ResponseEntity<Object> response = usersController.setPassword("2", Map.of("newPassword", "short"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "Password must be at least 8 characters"));
    }

    private ResponseEntity<Object> login() {
        HttpServletRequest http = mock(HttpServletRequest.class);
        when(http.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7");
        return auth.login(new LoginRequest("ana", "pw"), mock(BindingResult.class), http);
    }
}
