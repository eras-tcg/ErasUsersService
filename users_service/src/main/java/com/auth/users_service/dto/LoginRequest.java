package com.auth.users_service.dto;

import com.auth.users_service.validation.ValidUsername;
import com.auth.users_service.validation.ValidPassword;


@lombok.Data
@lombok.NoArgsConstructor
public class LoginRequest {
    @ValidUsername
    private String username;

    @ValidPassword
    private String password;
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
