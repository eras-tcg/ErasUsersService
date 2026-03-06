package com.auth.users_service.service;

import com.auth.users_service.model.User;
import com.auth.users_service.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.auth.users_service.utils.JwtUtils;



@Service
public class AuthService {
    // This is where you would implement your authentication logic, such as validating user credentials,
    // generating JWT tokens, etc. For now, it's just a placeholder.
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        // Constructor injection of dependencies
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }


    public String login(String username, String password) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(password, existingUser.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!existingUser.isVerified()) {
            throw new RuntimeException("Account not verified. Please check your email.");
        }
        
        String token = jwtUtils.generateToken(existingUser);

        System.out.println("User logged in with username: " + username);

        return token;
    }
}