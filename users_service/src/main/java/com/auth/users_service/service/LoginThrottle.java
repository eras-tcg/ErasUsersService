package com.auth.users_service.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Slows down password guessing: after too many failed logins within WINDOW, a username (or a client IP)
 * is refused for LOCK_TIME. Kept in memory: a restart forgets the counters, which is acceptable for a
 * single instance, and each map is capped at MAX_ENTRIES so a flood of names or addresses cannot exhaust memory.
 */
@Component
public class LoginThrottle {

    public static final int MAX_FAILURES_PER_USERNAME = 10;
    public static final int MAX_FAILURES_PER_IP = 30;
    public static final Duration WINDOW = Duration.ofMinutes(15);
    public static final Duration LOCK_TIME = Duration.ofMinutes(15);
    static final int MAX_ENTRIES = 10_000;

    /** Failures of one key: counted from windowStart; lockedUntil is set once the limit is reached. */
    private static final class Attempts {
        Instant windowStart;
        int failures;
        Instant lockedUntil;

        Attempts(Instant now) {
            this.windowStart = now;
        }
    }

    private final Clock clock;
    private final Map<String, Attempts> byUsername = new HashMap<>();
    private final Map<String, Attempts> byIp = new HashMap<>();

    @Autowired
    public LoginThrottle() {
        this(Clock.systemUTC());
    }

    /** The clock is injectable so tests can move time forward. */
    LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    /** True while the username or the IP is locked out: the password is not even checked then. */
    public synchronized boolean isBlocked(String username, String ip) {
        Instant now = clock.instant();
        return isLocked(byUsername, usernameKey(username), now) || isLocked(byIp, ip, now);
    }

    public synchronized void recordFailure(String username, String ip) {
        Instant now = clock.instant();
        fail(byUsername, usernameKey(username), MAX_FAILURES_PER_USERNAME, now);
        fail(byIp, ip, MAX_FAILURES_PER_IP, now);
    }

    /** A correct login clears the username's counter (the IP's stays: one account says nothing about the others). */
    public synchronized void recordSuccess(String username) {
        byUsername.remove(usernameKey(username));
    }

    /**
     * Best-effort client address: requests arrive through CloudFront and nginx, so the first X-Forwarded-For entry
     * is the original client. The header can be forged by a client; the per-username limit still applies then.
     */
    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String first = forwarded.split(",")[0].trim();
            if (!first.isEmpty()) return first;
        }
        return request.getRemoteAddr();
    }

    /** Case-insensitive, so "Admin" and "admin" share a counter. */
    private static String usernameKey(String username) {
        return username == null ? "" : username.toLowerCase(Locale.ROOT);
    }

    private static boolean isLocked(Map<String, Attempts> map, String key, Instant now) {
        if (key == null) return false;
        Attempts a = map.get(key);
        return a != null && a.lockedUntil != null && now.isBefore(a.lockedUntil);
    }

    private static void fail(Map<String, Attempts> map, String key, int max, Instant now) {
        if (key == null) return;
        Attempts a = map.get(key);
        if (a == null || isStale(a, now)) {
            if (a == null && map.size() >= MAX_ENTRIES) evict(map, now);
            a = new Attempts(now);
            map.put(key, a);
        }
        a.failures++;
        if (a.failures >= max) a.lockedUntil = now.plus(LOCK_TIME);
    }

    /** The window has passed and no lock is running: the entry no longer counts. */
    private static boolean isStale(Attempts a, Instant now) {
        boolean windowOver = !now.isBefore(a.windowStart.plus(WINDOW));
        boolean lockOver = a.lockedUntil == null || !now.isBefore(a.lockedUntil);
        return windowOver && lockOver;
    }

    /** Drops stale entries; if the map is still full, drops the oldest one so memory stays bounded. */
    private static void evict(Map<String, Attempts> map, Instant now) {
        map.values().removeIf(a -> isStale(a, now));
        if (map.size() >= MAX_ENTRIES) {
            map.entrySet().stream()
                .min(Comparator.comparing((Map.Entry<String, Attempts> e) -> e.getValue().windowStart))
                .map(Map.Entry::getKey)
                .ifPresent(map::remove);
        }
    }

    /** Thrown when a login is refused because of too many failures; the message is shown to the user as is. */
    public static class TooManyAttemptsException extends RuntimeException {
        public TooManyAttemptsException() {
            super("Demasiados intentos fallidos. Intenta de nuevo en unos minutos.");
        }
    }
}
