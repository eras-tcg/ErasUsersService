package com.auth.users_service.service;
import java.util.List;

import com.auth.users_service.config.RolesProperties;
import com.auth.users_service.dto.CreateRoleRequest;
import com.auth.users_service.model.Permission;
import com.auth.users_service.model.Role;
import com.auth.users_service.repository.PermissionRepository;
import com.auth.users_service.repository.RoleRepository;
import com.auth.users_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.auth.users_service.dto.EditRoleRequest;

@Service
@lombok.RequiredArgsConstructor
public class RolesService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionsRepository;
    private final UserRepository userRepository;
    private final RolesProperties rolesProperties;


    public Role createRole(CreateRoleRequest request) {

        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Role already exists");
        }
        List<Permission> permissions;
        if (request.getPermissions() == null) {
            permissions = new ArrayList<>();
        } else {
            try{
                permissions = new ArrayList<>(permissionsRepository.findAll().stream()
                        .filter(permission -> request.getPermissions().contains(permission.getName()))
                        .toList());
            } catch (RuntimeException e) {
                throw new RuntimeException("Error fetching permissions");
            }
        }   


        if (permissions.size() != request.getPermissions().size()) {
            if (Boolean.TRUE.equals(request.getCreateMissingPermissions())) {
                List<String> missingPermissions = request.getPermissions().stream()
                        .filter(name -> permissions.stream().noneMatch(p -> p.getName().equals(name)))
                        .toList();
                for (String missingPermission : missingPermissions) {
                    Permission newPermission = new Permission(missingPermission);
                    permissionsRepository.save(newPermission);
                    permissions.add(newPermission);
                }
            } else {
                throw new RuntimeException("One or more permissions not found");
            }
        }
        Role role = new Role(request.getName());
        role.getPermissions().addAll(permissions);
        roleRepository.save(role);
        return role;
    }

    public Role readRole(String name) {
        Role role = roleRepository.findByName(name).orElseThrow(() -> new RuntimeException("Role not found"));
        return role;
    }

    public List<Role> readAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles;
    }

    public void deleteRole(String name) {
        if (name.equals(rolesProperties.getBaseRole())) {
            throw new RuntimeException("The base role cannot be deleted");
        }
        Role role = roleRepository.findByName(name).orElseThrow(() -> new RuntimeException("Role not found"));
        List<com.auth.users_service.model.User> usersWithRole = userRepository.findByRole(role);
        for (com.auth.users_service.model.User user : usersWithRole) {
            user.setRole(null);
            userRepository.save(user);
        }
        roleRepository.delete(role);
    }

    public Role editRole(EditRoleRequest request) {
        Role role = roleRepository.findByName(request.getName()).orElseThrow(() -> new RuntimeException("Role not found"));

        if (request.getNewName() != null && request.getName().equals(rolesProperties.getBaseRole())) {
            throw new RuntimeException("The base role cannot be renamed");
        }

        if (request.getNewName() != null) {
            if (roleRepository.existsByName(request.getNewName())) {
                throw new RuntimeException("Role with that name already exists");
            }
            role.setName(request.getNewName());
        }
        if (request.getPermissionsToAdd() != null) {
            List<Permission> permissionsToAdd = new ArrayList<>(permissionsRepository.findAll().stream()
                    .filter(permission -> request.getPermissionsToAdd().contains(permission.getName()))
                    .toList());

            if (permissionsToAdd.size() != request.getPermissionsToAdd().size()) {
                if (Boolean.TRUE.equals(request.getCreateMissingPermissions())) {
                    List<String> missingPermissions = request.getPermissionsToAdd().stream()
                            .filter(name -> permissionsToAdd.stream().noneMatch(p -> p.getName().equals(name)))
                            .toList();
                    for (String missingPermission : missingPermissions) {
                        Permission newPermission = new Permission(missingPermission);
                        permissionsRepository.save(newPermission);
                        permissionsToAdd.add(newPermission);
                    }
                } else {
                    throw new RuntimeException("One or more permissions not found");
                }
            }
            role.getPermissions().addAll(permissionsToAdd);
        }
        if (request.getPermissionsToRemove() != null) {
            List<Permission> permissionsToRemove = role.getPermissions().stream()
                    .filter(permission -> request.getPermissionsToRemove().contains(permission.getName()))
                    .toList();
            role.getPermissions().removeAll(permissionsToRemove);
        }
        roleRepository.save(role);
        return role;
    }
}
