package com.auth.users_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;


@Document(collection = "users")
@lombok.Data
@lombok.NoArgsConstructor
public class User implements UserDetails {

    @Id
    private String id;

    @DBRef
    private Role role;

    private String username;
    private String email;
    private String password;
    private Integer tokenVersion;
    private boolean verified;
    private String verificationToken;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.tokenVersion = 0;
        this.verified = false;
        this.verificationToken = UUID.randomUUID().toString();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

}
