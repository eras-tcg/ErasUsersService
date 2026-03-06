package com.auth.users_service.controller;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.auth.users_service.model.Permission;
import com.auth.users_service.service.PermissionsService;
import com.auth.users_service.dto.CreatePermissionRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
@ConditionalOnProperty(prefix = "roles", name = "allowed", havingValue = "true")
public class PermissionsController {
    
    private final PermissionsService permissionsService;

    public PermissionsController(PermissionsService permissionsService) {
        this.permissionsService = permissionsService;
    }

    @PostMapping("/permissions")
    public ResponseEntity<Object> createPermission(@RequestBody CreatePermissionRequest request) {
        try{
            return ResponseEntity.ok(permissionsService.createPermission(request.getName()));
        }
        catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/permissions/{name}")
    public ResponseEntity<Object> readPermission(@PathVariable String name) {
        try{
            return ResponseEntity.ok(permissionsService.readPermission(name));
        }
        catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/permissions")
    public ResponseEntity<Object> readAllPermissions() {
        try{
            List<Permission> response = permissionsService.readAllPermissions();
            return ResponseEntity.ok(response);
        }
        catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/permissions/{id}")
    public ResponseEntity<Object> deletePermission(@PathVariable String id) {
        try{
            permissionsService.deletePermission(id);
            return ResponseEntity.ok().build();
        }
        catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/permissions/{id}")
    public ResponseEntity<Object> editPermission(@PathVariable String id, @RequestBody Map<String, String> body) {
        try{
            Permission response = permissionsService.editPermission(id, body.get("newName"));
            return ResponseEntity.ok(response);
        }
        catch(RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
    
}
