package com.auth.users_service.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.auth.users_service.model.User;
import com.auth.users_service.config.JwtProperties;

@Component
public class JwtUtils {

    private JwtProperties jwtProperties;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {

        SecretKey key = getSigningKey();

        String token = Jwts.builder()
        .subject(user.getId())
        .issuedAt(new Date())
        .expiration(new Date((new Date()).getTime() + jwtProperties.getExpiration()))

        .signWith(key)
        .claim("token_version", user.getTokenVersion())
        .compact();

        return token;
    }

    public boolean isTokenValid(String token) {

        try {
            extractId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractId(String token) {
        
        SecretKey key = getSigningKey();

        try {
            String username = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return username;
        } catch (Exception e) {
            if (e instanceof io.jsonwebtoken.ExpiredJwtException) {
                throw new RuntimeException("JWT token has expired");
            } else {
                throw new RuntimeException("Invalid JWT token");
            }
        } 
    }

    public int extractTokenVersion(String token) {
        SecretKey key = getSigningKey();

        try {
            Integer tokenVersion = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("token_version", Integer.class);

            return tokenVersion;
        } catch (Exception e) {
            if (e instanceof io.jsonwebtoken.ExpiredJwtException) {
                throw new RuntimeException("JWT token has expired");
            } else {
                throw new RuntimeException("Invalid JWT token");
            }
        } 
    }
}
    