package com.auth.users_service.dto;
import java.util.List;


@lombok.Data
@lombok.NoArgsConstructor
public class EditRoleRequest {

    private String name;
    private List<String> permissionsToAdd;
    private List<String> permissionsToRemove;
    private Boolean createMissingPermissions;

    private String newName;

    public EditRoleRequest(String name, String newName, List<String> permissionsToAdd, List<String> permissionsToRemove, Boolean createMissingPermissions) {
        this.name = name;
        this.newName = newName;
        this.permissionsToAdd = permissionsToAdd;
        this.permissionsToRemove = permissionsToRemove;
        this.createMissingPermissions = createMissingPermissions;
    }
}
