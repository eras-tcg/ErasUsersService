package com.auth.users_service.dto;


@lombok.Data
@lombok.NoArgsConstructor
public class CreatePermissionRequest {

    private String name;

    public CreatePermissionRequest(String name) {
        this.name = name;
    }
}
