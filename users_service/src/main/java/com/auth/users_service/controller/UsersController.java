package com.auth.users_service.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.users_service.dto.ChangePasswordRequest;
import com.auth.users_service.dto.EditUserRequest;
import com.auth.users_service.service.UsersManagerService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api")
public class UsersController {
    
    
    private final UsersManagerService usersManagerService;

    public UsersController (UsersManagerService usersManagerService) {
        this.usersManagerService = usersManagerService;
    }
    
    @GetMapping("/users")
    public ResponseEntity<Object> getUsers() {
    System.out.println("Get users endpoint hitted");
        try{
            return ResponseEntity.ok(usersManagerService.getAllUsers());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
        
    }

    @DeleteMapping("/users/delete/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable String id) {
        try {
            usersManagerService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/users/change-password")
    public ResponseEntity<Object> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                                        BindingResult bindingResult){

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            String token = usersManagerService.changePassword(request);

            return ResponseEntity.ok("Password changed successfully: "+ token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/users/update/{id}")
    public ResponseEntity<Object> updateUser(@PathVariable String id, @RequestBody EditUserRequest request) {
        try{
            String token = usersManagerService.updateUser(id, request);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    

}
