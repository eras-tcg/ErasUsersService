package com.auth.users_service.service;

import java.util.UUID;

import com.auth.users_service.model.User;
import com.auth.users_service.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.auth.users_service.utils.JwtUtils;



@Service
public class AuthService {
    // This is where you would implement your authentication logic, such as validating user credentials,
    // generating JWT tokens, etc. For now, it's just a placeholder.

    /** Same answer for an unknown username and a wrong password, so a login does not reveal which accounts exist. */
    public static final String INVALID_CREDENTIALS = "Usuario o contraseña incorrectos";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final LoginThrottle loginThrottle;
    private final LoginActivity loginActivity;

    /** Checked when the username does not exist, so that case takes as long as a wrong password. */
    private final String dummyHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, LoginThrottle loginThrottle,
                       LoginActivity loginActivity) {
        // Constructor injection of dependencies
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.loginThrottle = loginThrottle;
        this.loginActivity = loginActivity;
        this.dummyHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }


    public String login(String username, String password, String clientIp) {
        if (loginThrottle.isBlocked(username, clientIp)) {
            throw new LoginThrottle.TooManyAttemptsException();
        }

        User existingUser = userRepository.findByUsername(username);
        String hash = existingUser != null ? existingUser.getPassword() : dummyHash;
        boolean passwordMatches = passwordEncoder.matches(password, hash);

        if (existingUser == null || !passwordMatches) {
            loginThrottle.recordFailure(username, clientIp);
            throw new BadCredentialsException(INVALID_CREDENTIALS);
        }

        // Only said after a correct password, so it reveals nothing to someone guessing
        if (!existingUser.isVerified()) {
            throw new RuntimeException("Account not verified. Please check your email.");
        }

        loginThrottle.recordSuccess(username);
        String token = jwtUtils.generateToken(existingUser);
        loginActivity.record(existingUser.getId());

        System.out.println("User logged in with username: " + username);

        return token;
    }
}
