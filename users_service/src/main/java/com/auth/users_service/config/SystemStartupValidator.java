package com.auth.users_service.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.auth.users_service.model.Role;
import com.auth.users_service.model.User;
import com.auth.users_service.repository.RoleRepository;
import com.auth.users_service.repository.UserRepository;


@Component
public class SystemStartupValidator {

    private final RolesProperties rolesProperties;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemStartupValidator(RolesProperties rolesProperties,
                                    RoleRepository roleRepository,
                                    UserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        this.rolesProperties = rolesProperties;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationEvent() {
        String baseUsername = rolesProperties.getBaseUsername();

        if (rolesProperties.getAllowed()) {
            String baseRoleName = rolesProperties.getBaseRole();

            Role role = roleRepository.findByName(baseRoleName).orElseGet(() -> {
                Role r = new Role(baseRoleName);
                return roleRepository.save(r);
            });

            if (userRepository.findByUsername(baseUsername) == null) {
                User user = new User(baseUsername, null, passwordEncoder.encode(rolesProperties.getBasePassword()));
                user.setRole(role);
                userRepository.save(user);
            }
        } else {
            if (userRepository.findByUsername(baseUsername) == null) {
                User user = new User(baseUsername, null, passwordEncoder.encode(rolesProperties.getBasePassword()));
                userRepository.save(user);
            }
        }
    }

}
