package com.auth.users_service.controller;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.auth.users_service.dto.CreateRoleRequest;
import com.auth.users_service.dto.EditRoleRequest;
import com.auth.users_service.model.Role;
import com.auth.users_service.service.RolesService;


@RestController
@RequestMapping("/api")
@ConditionalOnProperty(prefix = "roles", name = "allowed", havingValue = "true")
public class RolesController {
    
    private final RolesService rolesService;

    public RolesController(RolesService rolesService) {
        this.rolesService = rolesService;
    }

    @PostMapping("/roles")
    public ResponseEntity<Object> createRole(@RequestBody CreateRoleRequest request) {
        try{
            Role response = rolesService.createRole(request);
            return ResponseEntity.ok(response);
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/roles/{name}")
    public ResponseEntity<Role> readRole(@PathVariable String name) {
        Role response = rolesService.readRole(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> readAllRoles() {
        List<Role> response = rolesService.readAllRoles();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/roles/{name}")
    public ResponseEntity<Object> deleteRole(@PathVariable String name) {
        try{
            rolesService.deleteRole(name);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }

    }

    @PutMapping("/roles")
    public ResponseEntity<Object> editRole(@RequestBody EditRoleRequest request) {
        try{
            Role response = rolesService.editRole(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
}
