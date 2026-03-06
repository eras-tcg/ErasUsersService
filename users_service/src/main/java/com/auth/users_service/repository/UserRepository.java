package com.auth.users_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.auth.users_service.model.User;
import java.util.List;


public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByVerificationToken(String verificationToken);
    List<User> findAll();
    void deleteByUsername(String username);
    List<User> findByRole(com.auth.users_service.model.Role role);
}
