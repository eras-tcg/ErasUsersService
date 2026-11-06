package com.auth.users_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth.users_service.model.User;
import com.auth.users_service.repository.UserRepository;
import com.auth.users_service.utils.JwtUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** A failed login says the same thing whether the username exists or not, and is throttled. */
class AuthServiceTest {

    private static final String DUMMY_HASH = "$2a$10$dummy";

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtUtils jwt = mock(JwtUtils.class);
    private final LoginThrottle throttle = mock(LoginThrottle.class);
    private final LoginActivity activity = mock(LoginActivity.class);
    private AuthService service;

    @BeforeEach
    void setUp() {
        when(encoder.encode(anyString())).thenReturn(DUMMY_HASH);
        service = new AuthService(users, encoder, jwt, throttle, activity);
    }

    @Test
    void anUnknownUserAndAWrongPasswordGetTheSameAnswer() {
        User ana = verifiedUser("ana", "hash");
        when(users.findByUsername("ana")).thenReturn(ana);

        assertThatThrownBy(() -> service.login("nobody", "pw", "ip"))
            .isInstanceOf(BadCredentialsException.class).hasMessage(AuthService.INVALID_CREDENTIALS);
        assertThatThrownBy(() -> service.login("ana", "wrong", "ip"))
            .isInstanceOf(BadCredentialsException.class).hasMessage(AuthService.INVALID_CREDENTIALS);

        verify(throttle).recordFailure("nobody", "ip");
        verify(throttle).recordFailure("ana", "ip");
        verify(activity, never()).record(any());
    }

    @Test
    void anUnknownUserStillRunsAPasswordCheckSoItTakesAsLong() {
        assertThatThrownBy(() -> service.login("nobody", "pw", "ip")).isInstanceOf(BadCredentialsException.class);
        verify(encoder).matches("pw", DUMMY_HASH);
    }

    @Test
    void aBlockedLoginIsRefusedWithoutCheckingThePassword() {
        when(throttle.isBlocked("ana", "ip")).thenReturn(true);

        assertThatThrownBy(() -> service.login("ana", "pw", "ip"))
            .isInstanceOf(LoginThrottle.TooManyAttemptsException.class);
        verify(encoder, never()).matches(any(), any());
    }

    @Test
    void aCorrectLoginClearsTheCounterAndReturnsAToken() {
        User ana = verifiedUser("ana", "hash");
        when(users.findByUsername("ana")).thenReturn(ana);
        when(encoder.matches("pw", "hash")).thenReturn(true);
        when(jwt.generateToken(ana)).thenReturn("token");

        assertThat(service.login("ana", "pw", "ip")).isEqualTo("token");
        verify(throttle).recordSuccess("ana");
        verify(activity).record(ana.getId());
    }

    @Test
    void notVerifiedIsOnlySaidAfterTheRightPassword() {
        User ana = new User("ana", "a@x.cl", "hash");
        when(users.findByUsername("ana")).thenReturn(ana);
        when(encoder.matches("pw", "hash")).thenReturn(true);

        assertThatThrownBy(() -> service.login("ana", "pw", "ip")).hasMessageContaining("not verified");
        assertThatThrownBy(() -> service.login("ana", "wrong", "ip")).hasMessage(AuthService.INVALID_CREDENTIALS);
    }

    private static User verifiedUser(String username, String hash) {
        User user = new User(username, username + "@x.cl", hash);
        user.setVerified(true);
        return user;
    }
}
