package com.auth.users_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.auth.users_service.validation.ValidUsername;
import com.auth.users_service.validation.ValidPassword;

@lombok.Data
@lombok.NoArgsConstructor
public class UserRegistrationRequest {

    @ValidUsername
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @ValidPassword
    private String password;

    public UserRegistrationRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
