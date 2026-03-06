package com.auth.users_service.repository;

import com.auth.users_service.model.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;


public interface PermissionRepository extends MongoRepository<Permission, String> {
    boolean existsByName(String name);
    List<Permission> findAll();
    Permission findByName(String name);
    void deleteByName(String name);
}
