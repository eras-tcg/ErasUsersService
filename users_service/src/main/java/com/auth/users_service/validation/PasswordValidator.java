package com.auth.users_service.validation;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import com.auth.users_service.config.UserProperties;



@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, String>  {

    private UserProperties.Password passwordProperties;

    public PasswordValidator(UserProperties userProperties) {
        this.passwordProperties = userProperties.getPassword();
    }


    @Override
    public boolean isValid(String value, jakarta.validation.ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Password is required").addConstraintViolation();
            return false;
        }
        if (value.length() < passwordProperties.getMinLength() || value.length() > passwordProperties.getMaxLength()) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Password must be between " + passwordProperties.getMinLength() + " and " + passwordProperties.getMaxLength() + " characters").addConstraintViolation();
            return false;
        }
        if (passwordProperties.isHasUppercase() && !value.matches(".*[A-Z].*")) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Password must contain at least one uppercase letter").addConstraintViolation();
            return false;
        }
        if (passwordProperties.isHasLowercase() && !value.matches(".*[a-z].*")) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Password must contain at least one lowercase letter").addConstraintViolation();
            return false;
        }
        if (passwordProperties.isHasDigit() && !value.matches(".*\\d.*")) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Password must contain at least one digit").addConstraintViolation();
            return false;
        }
        if (passwordProperties.isHasSpecial()) {
            boolean hasSpecial = passwordProperties.getSpecialChars().chars()
                .anyMatch(c -> value.indexOf(c) >= 0);
            if (!hasSpecial) {
                ctx.disableDefaultConstraintViolation();
                ctx.buildConstraintViolationWithTemplate(
                    "Password must contain at least one special character (" + passwordProperties.getSpecialChars() + ")"
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
    
}
