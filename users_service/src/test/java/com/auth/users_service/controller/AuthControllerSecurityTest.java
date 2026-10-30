package com.auth.users_service.controller;

import com.auth.users_service.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;

import static org.assertj.core.api.Assertions.assertThat;

/** Accounts are private: registering one needs manage_users (it is not among the open /api/auth routes either). */
class AuthControllerSecurityTest {

    @Test
    void registeringNeedsManageUsers() throws Exception {
        PreAuthorize rule = AuthController.class
                .getMethod("register", UserRegistrationRequest.class, BindingResult.class)
                .getAnnotation(PreAuthorize.class);
        assertThat(rule).isNotNull();
        assertThat(rule.value()).isEqualTo("hasAuthority('manage_users')");
    }
}
