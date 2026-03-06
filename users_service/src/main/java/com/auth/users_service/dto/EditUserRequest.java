package com.auth.users_service.dto;


@lombok.Data
@lombok.NoArgsConstructor
public class EditUserRequest {

    private String username;
    private String email;
    private String oldPassword;
    private String newPassword;
    private String roleId;

}
