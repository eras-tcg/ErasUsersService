package com.auth.users_service.repository;

import com.auth.users_service.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;


public interface RoleRepository extends MongoRepository<Role, String> {
    boolean existsByName(String name);
    List<Role> findAll();
    Optional<Role> findByName(String name);
    void deleteByName(String name);
    List<Role> findByPermissionsContaining(com.auth.users_service.model.Permission permission);
}
