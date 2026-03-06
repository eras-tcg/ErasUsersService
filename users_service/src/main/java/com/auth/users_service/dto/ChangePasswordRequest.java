package com.auth.users_service.dto;
import com.auth.users_service.validation.ValidPassword;


@lombok.Data
@lombok.NoArgsConstructor
public class ChangePasswordRequest {

    private String username; // Optional, only for admins to change other users' passwords
    
    private String oldPassword;
    
    @ValidPassword
    private String newPassword;

    public ChangePasswordRequest(String username, String oldPassword, String newPassword) {
        this.username = username;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.username = null;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
