package com.auth.users_service.service;

import com.auth.users_service.config.RolesProperties;
import com.auth.users_service.dto.CheckAccessRequest;
import com.auth.users_service.model.Role;
import com.auth.users_service.model.User;
import com.auth.users_service.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@lombok.RequiredArgsConstructor
public class CheckAccessService {

    private final RolesProperties rolesProperties;
    private final UserRepository userRepository;

    public boolean checkAccess(CheckAccessRequest request) {
        boolean hasRole = request.getRole() != null && !request.getRole().isBlank();
        boolean hasPermissions = request.getPermissions() != null && !request.getPermissions().isEmpty();

        if (!hasRole && !hasPermissions) {
            throw new RuntimeException("At least one of role or permissions must be provided");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!rolesProperties.getAllowed()) {
            return username.equals(rolesProperties.getBaseUsername());
        }

        User user = userRepository.findByUsername(username);
        if (user == null || user.getRole() == null) return false;

        Role userRole = user.getRole();

        if (hasRole && !userRole.getName().equals(request.getRole())) return false;

        if (hasPermissions) {
            List<String> userPermissionNames = userRole.getPermissions().stream()
                    .map(p -> p.getName()).toList();
            if (!userPermissionNames.containsAll(request.getPermissions())) return false;
        }

        return true;
    }
}
