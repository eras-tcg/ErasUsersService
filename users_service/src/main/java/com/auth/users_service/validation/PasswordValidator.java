package com.auth.users_service.validation;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * A password only needs MIN_LENGTH characters (no required kinds of character). MAX_LENGTH is a technical cap:
 * BCrypt reads only the first 72 bytes of a password.
 */
@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        String problem = problem(value);
        return problem == null || refuse(ctx, problem);
    }

    /** Why a password breaks the policy, or null if it is fine; for code that sets a password outside bean validation. */
    public static String problem(String value) {
        if (value == null || value.isEmpty()) return "Password is required";
        if (value.length() < MIN_LENGTH) return "Password must be at least " + MIN_LENGTH + " characters";
        if (value.length() > MAX_LENGTH) return "Password must be at most " + MAX_LENGTH + " characters";
        return null;
    }

    private static boolean refuse(ConstraintValidatorContext ctx, String message) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }
}
