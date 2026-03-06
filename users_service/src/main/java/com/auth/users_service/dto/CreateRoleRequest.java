package com.auth.users_service.dto;
import java.util.List;


@lombok.Data
@lombok.NoArgsConstructor
public class CreateRoleRequest {

    private String name;
    private List<String> permissions;
    private Boolean createMissingPermissions;

    public CreateRoleRequest(String name, List<String> permissions, Boolean createMissingPermissions) {
        this.name = name;
        this.permissions = permissions;
        this.createMissingPermissions = createMissingPermissions;
    }

    public List<String> getPermissions() {
        if (this.permissions == null) {
            return List.of();
        }

        return this.permissions;
    }
}
