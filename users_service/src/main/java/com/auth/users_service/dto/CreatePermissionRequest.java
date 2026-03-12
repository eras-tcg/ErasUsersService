package com.auth.users_service.dto;


@lombok.Data
@lombok.NoArgsConstructor
public class CreatePermissionRequest {

    private String name;
    private String description;

    public CreatePermissionRequest(String name) {
        this.name = name;
    }

    public CreatePermissionRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
