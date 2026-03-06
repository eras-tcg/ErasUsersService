package com.auth.users_service.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.auth.users_service.dto.UserRegistrationRequest;
import com.auth.users_service.dto.LoginRequest;
import com.auth.users_service.dto.CheckAccessRequest;
import com.auth.users_service.service.AuthService;
import com.auth.users_service.service.CheckAccessService;
import com.auth.users_service.service.UsersManagerService;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsersManagerService usersManagerService;
    private final CheckAccessService checkAccessService;


    public AuthController(AuthService authService, UsersManagerService usersManagerService, CheckAccessService checkAccessService) {
        this.authService = authService;
        this.usersManagerService = usersManagerService;
        this.checkAccessService = checkAccessService;
    }

    @GetMapping("/verify")
    public ResponseEntity<Object> verify(@RequestParam String token) {
        try {
            usersManagerService.verifyUser(token);
            return ResponseEntity.ok("Account verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        System.out.println("Health endpoint hitted");
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody UserRegistrationRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            String username = usersManagerService.register(request);
            return ResponseEntity.ok("User registered successfully: " + username);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
        System.out.println("Login endpoint hitted with username: " + request.getUsername());

        try {
            String token = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/check-access")
    public ResponseEntity<Object> checkAccess(@RequestBody CheckAccessRequest request) {
        try {
            boolean hasAccess = checkAccessService.checkAccess(request);
            return ResponseEntity.ok(Map.of("hasAccess", hasAccess));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


}