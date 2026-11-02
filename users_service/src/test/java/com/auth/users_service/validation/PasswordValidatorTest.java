package com.auth.users_service.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** A password only needs 8 characters: no uppercase, digit or special character is required. */
class PasswordValidatorTest {

    private final PasswordValidator validator = new PasswordValidator();

    @Test
    void eightCharactersOfAnyKindAreEnough() {
        assertThat(validator.isValid("abcdefgh", context())).isTrue();
        assertThat(validator.isValid("12345678", context())).isTrue();
    }

    @Test
    void aShorterOneIsRefusedSayingWhy() {
        ConstraintValidatorContext ctx = context();

        assertThat(validator.isValid("abcdefg", ctx)).isFalse();
        verify(ctx).buildConstraintViolationWithTemplate("Password must be at least 8 characters");
    }

    @Test
    void anEmptyOrTooLongOneIsRefused() {
        assertThat(validator.isValid("", context())).isFalse();
        assertThat(validator.isValid(null, context())).isFalse();
        assertThat(validator.isValid("a".repeat(65), context())).isFalse();
    }

    private static ConstraintValidatorContext context() {
        return mock(ConstraintValidatorContext.class, RETURNS_DEEP_STUBS);
    }
}
