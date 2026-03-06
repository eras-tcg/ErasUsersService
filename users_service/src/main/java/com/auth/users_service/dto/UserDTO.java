package com.auth.users_service.dto;


@lombok.Data
@lombok.AllArgsConstructor
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String role;

}
