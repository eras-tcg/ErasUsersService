package com.auth.users_service.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** After 10 failures per username (30 per IP) within 15 minutes, logins are refused for 15 minutes. */
class LoginThrottleTest {

    /** A clock the test moves by hand. */
    private static final class TestClock extends Clock {
        Instant now = Instant.parse("2026-11-02T12:00:00Z");

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
        void advance(Duration d) { now = now.plus(d); }
    }

    private final TestClock clock = new TestClock();
    private final LoginThrottle throttle = new LoginThrottle(clock);

    @Test
    void theTenthFailureLocksTheUsernameForFifteenMinutes() {
        for (int i = 0; i < 9; i++) throttle.recordFailure("ana", "ip" + i);
        assertThat(throttle.isBlocked("ana", "other")).isFalse();

        throttle.recordFailure("ana", "ip9");
        assertThat(throttle.isBlocked("ana", "other")).isTrue();
        assertThat(throttle.isBlocked("ANA", "other")).as("case does not dodge the lock").isTrue();
        assertThat(throttle.isBlocked("bob", "other")).isFalse();

        clock.advance(Duration.ofMinutes(15));
        assertThat(throttle.isBlocked("ana", "other")).isFalse();
    }

    @Test
    void failuresOutsideTheWindowDoNotAddUp() {
        for (int i = 0; i < 9; i++) throttle.recordFailure("ana", "ip");
        clock.advance(Duration.ofMinutes(16));
        throttle.recordFailure("ana", "ip");
        assertThat(throttle.isBlocked("ana", "x")).isFalse();
    }

    @Test
    void anIpIsLockedAfterThirtyFailuresOnAnyUsernames() {
        for (int i = 0; i < 29; i++) throttle.recordFailure("user" + i, "1.2.3.4");
        assertThat(throttle.isBlocked("someone", "1.2.3.4")).isFalse();

        throttle.recordFailure("user29", "1.2.3.4");
        assertThat(throttle.isBlocked("someone", "1.2.3.4")).isTrue();
        assertThat(throttle.isBlocked("someone", "5.6.7.8")).isFalse();
    }

    @Test
    void aSuccessClearsTheUsernameCounter() {
        for (int i = 0; i < 9; i++) throttle.recordFailure("ana", "ip");
        throttle.recordSuccess("ana");
        throttle.recordFailure("ana", "ip");
        assertThat(throttle.isBlocked("ana", "x")).isFalse();
    }

    @Test
    void memoryStaysBounded() {
        for (int i = 0; i < LoginThrottle.MAX_ENTRIES + 500; i++) throttle.recordFailure("user" + i, "ip" + i);
        // The newest entries are kept; the oldest were dropped to make room
        for (int i = 0; i < 9; i++) throttle.recordFailure("user" + (LoginThrottle.MAX_ENTRIES + 499), "x");
        assertThat(throttle.isBlocked("user" + (LoginThrottle.MAX_ENTRIES + 499), "y")).isTrue();
    }

    @Test
    void theClientIpIsTheFirstForwardedAddressElseTheRemoteOne() {
        HttpServletRequest forwarded = mock(HttpServletRequest.class);
        when(forwarded.getHeader("X-Forwarded-For")).thenReturn("203.0.113.7, 10.0.0.2");
        when(forwarded.getRemoteAddr()).thenReturn("10.0.0.3");
        assertThat(LoginThrottle.clientIp(forwarded)).isEqualTo("203.0.113.7");

        HttpServletRequest direct = mock(HttpServletRequest.class);
        when(direct.getRemoteAddr()).thenReturn("10.0.0.3");
        assertThat(LoginThrottle.clientIp(direct)).isEqualTo("10.0.0.3");
    }
}
