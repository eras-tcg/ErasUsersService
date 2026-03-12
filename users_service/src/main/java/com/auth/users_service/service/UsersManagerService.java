package com.auth.users_service.service;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.auth.users_service.repository.UserRepository;
import com.auth.users_service.repository.RoleRepository;
import com.auth.users_service.dto.UserRegistrationRequest;
import com.auth.users_service.model.User;
import com.auth.users_service.model.Role;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth.users_service.config.RolesProperties;
import com.auth.users_service.config.UserProperties;
import com.auth.users_service.dto.ChangePasswordRequest;
import com.auth.users_service.dto.EditUserRequest;
import com.auth.users_service.dto.UserDTO;
import com.auth.users_service.utils.JwtUtils;

@Service
@lombok.RequiredArgsConstructor
public class UsersManagerService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserProperties userProperties;
    private final RolesProperties rolesProperties;
    private final EmailService emailService;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getEmail(), u.getRole() != null ? u.getRole().getName() : null, u.isVerified()))
            .toList();
    }

    public void deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUsername().equals(rolesProperties.getBaseUsername())) {
            throw new RuntimeException("The base user cannot be deleted");
        }

        if (!userProperties.isCanDeleteOwnAccount()) {
            String acting_user = SecurityContextHolder.getContext().getAuthentication().getName();
            if (acting_user.equals(user.getUsername())) {
                throw new RuntimeException("You can't delete your own account");
            }
        }
        userRepository.deleteById(id);
    }

    public String register(UserRegistrationRequest request) {
        User existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Username already exists");
        }

        User existingEmail = userRepository.findByEmail(request.getEmail());
        if (existingEmail != null) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User(request.getUsername(), request.getEmail(), passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByName("USER")
            .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());

        System.out.println("Registering user with username: " + request.getUsername());

        return user.getUsername();
    }

    public void verifyUser(String token) {
        User user = userRepository.findByVerificationToken(token);
        if (user == null) {
            throw new RuntimeException("Invalid verification token");
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }


    private User validatePassword(String oldPassword, String newPassword, User user) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password cannot be the same as the old password");
        }
        user.setPassword(passwordEncoder.encode(newPassword));

        user.setTokenVersion(user.getTokenVersion()+1);

        return user;

    }

    public String changePassword(ChangePasswordRequest request) {
        String username;
        if (request.getUsername() == null) {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        } else {
            username = request.getUsername();
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user = validatePassword(request.getOldPassword(), request.getNewPassword(), user);

        String token = jwtUtils.generateToken(user);

        userRepository.save(user);

        System.out.println("Password changed for user: " + username);
        return token;
    }

    public String updateUser(String id, EditUserRequest request) {

        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getUsername().equals(rolesProperties.getBaseUsername())) {
            throw new RuntimeException("The base user cannot be edited");
        }

        User UserFoundByUsername = userRepository.findByUsername(request.getUsername());

        if (UserFoundByUsername != null && !UserFoundByUsername.getId().equals(id)) {
            throw new RuntimeException("Username already exists");
        }

        User UserFoundByEmail = userRepository.findByEmail(request.getEmail());
        if (UserFoundByEmail != null && !UserFoundByEmail.getId().equals(id)) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        } else {
            user.setRole(null);
        }

        if (request.getOldPassword() != null && request.getNewPassword() != null) {
            user = validatePassword(request.getOldPassword(), request.getNewPassword(), user);
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }
        else if (request.getOldPassword() != null || request.getNewPassword() != null) {
            throw new RuntimeException("Both old and new passwords must be provided");
        }
        else {
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            userRepository.save(user);
        }

        return jwtUtils.generateToken(user);
    }

    public void setPassword(String id, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

}
