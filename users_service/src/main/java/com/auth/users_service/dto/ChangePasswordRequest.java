package com.auth.users_service.dto;
import com.auth.users_service.validation.ValidPassword;


/**
 * Changes the signed-in user's own password. There is no username: the account always comes from the session,
 * and a "username" sent by an older client is ignored.
 */
@lombok.Data
@lombok.NoArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class ChangePasswordRequest {

    private String oldPassword;
    
    @ValidPassword
    private String newPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
