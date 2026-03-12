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

            User existingUser = userRepository.findByUsername(baseUsername);
            if (existingUser == null) {
                User user = new User(baseUsername, rolesProperties.getBaseEmail(), passwordEncoder.encode(rolesProperties.getBasePassword()));
                user.setRole(role);
                user.setVerified(true);
                user.setVerificationToken(null);
                userRepository.save(user);
            } else {
                boolean changed = false;
                if (existingUser.getRole() == null) {
                    existingUser.setRole(role);
                    changed = true;
                }
                if (!existingUser.isVerified()) {
                    existingUser.setVerified(true);
                    existingUser.setVerificationToken(null);
                    changed = true;
                }
                if (changed) {
                    userRepository.save(existingUser);
                }
            }
        } else {
            User existingUser = userRepository.findByUsername(baseUsername);
            if (existingUser == null) {
                User user = new User(baseUsername, rolesProperties.getBaseEmail(), passwordEncoder.encode(rolesProperties.getBasePassword()));
                user.setVerified(true);
                user.setVerificationToken(null);
                userRepository.save(user);
            } else if (!existingUser.isVerified()) {
                existingUser.setVerified(true);
                existingUser.setVerificationToken(null);
                userRepository.save(existingUser);
            }
        }
    }

}
