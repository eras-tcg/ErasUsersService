package com.auth.users_service.dto;

import com.auth.users_service.validation.ValidUsername;

import jakarta.validation.constraints.NotBlank;


@lombok.Data
@lombok.NoArgsConstructor
public class LoginRequest {
    @ValidUsername
    private String username;

    /** Only required here: the password policy applies when a password is set, not to existing ones signing in. */
    @NotBlank(message = "Password is required")
    private String password;
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
