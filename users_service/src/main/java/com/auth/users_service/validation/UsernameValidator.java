package com.auth.users_service.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import com.auth.users_service.config.UserProperties;



@Component
public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private UserProperties.Username usernameProperties;

    public UsernameValidator(UserProperties userProperties) {
        this.usernameProperties = userProperties.getUsername();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Username is required").addConstraintViolation();
            return false;
        }
        if (value.length() < usernameProperties.getMinLength() || value.length() > usernameProperties.getMaxLength()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                "Username must be between " + usernameProperties.getMinLength() + " and " + usernameProperties.getMaxLength() + " characters"
            ).addConstraintViolation();
            return false;
        }
        if (!value.matches("^[a-zA-Z0-9_]+$")) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(
                "Username can only contain letters, numbers, and underscores"
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}
