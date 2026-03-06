
package com.auth.users_service.service;

import org.springframework.stereotype.Service;
import com.auth.users_service.repository.PermissionRepository;
import com.auth.users_service.repository.RoleRepository;
import com.auth.users_service.model.Permission;
import com.auth.users_service.model.Role;
import java.util.List;


@Service
@lombok.RequiredArgsConstructor
public class PermissionsService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public Permission createPermission(String request) {
        if (permissionRepository.existsByName(request)) {
            throw new RuntimeException("Permission already exists");
        }
        Permission permission = new Permission(request);
        permissionRepository.save(permission);
        return permission;
    }

    public Permission readPermission(String name) {
        Permission permission = permissionRepository.findByName(name);
        if (permission == null) {
            throw new RuntimeException("Permission not found");
        }
        return permission;
    }

    public List<Permission> readAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions;
    }

    public void deletePermission(String id) {
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission == null) {
            throw new RuntimeException("Permission not found");
        }
        List<Role> rolesWithPermission = roleRepository.findByPermissionsContaining(permission);
        for (Role role : rolesWithPermission) {
            role.getPermissions().removeIf(p -> p.getId().equals(permission.getId()));
            roleRepository.save(role);
        }
        permissionRepository.delete(permission);
    }

    public Permission editPermission(String id, String newName) {
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission == null) {
            throw new RuntimeException("Permission not found");
        }
        if (permissionRepository.existsByName(newName)) {
            throw new RuntimeException("Permission with the new name already exists");
        }
        permission.setName(newName);
        permissionRepository.save(permission);
        return permission;
    }
}
