package com.example.directoriofuncionarios.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration LOCK_DURATION = Duration.ofSeconds(30);

    private final Clock clock;
    private final Map<String, AttemptState> attempts = new HashMap<>();

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public synchronized long remainingLockSeconds(String username) {
        AttemptState state = attempts.get(username);
        if (state == null || state.lockedUntil() == null) {
            return 0;
        }

        long remainingSeconds = secondsUntil(state.lockedUntil());
        if (remainingSeconds <= 0) {
            attempts.remove(username);
            return 0;
        }
        return remainingSeconds;
    }

    public synchronized AttemptResult recordFailure(String username) {
        long lockRemaining = remainingLockSeconds(username);
        if (lockRemaining > 0) {
            return new AttemptResult(0, lockRemaining);
        }

        AttemptState current = attempts.get(username);
        int failures = current == null ? 1 : current.failures() + 1;
        if (failures >= MAX_ATTEMPTS) {
            Instant lockedUntil = clock.instant().plus(LOCK_DURATION);
            attempts.put(username, new AttemptState(0, lockedUntil));
            return new AttemptResult(0, LOCK_DURATION.toSeconds());
        }

        attempts.put(username, new AttemptState(failures, null));
        return new AttemptResult(MAX_ATTEMPTS - failures, 0);
    }

    public synchronized void reset(String username) {
        attempts.remove(username);
    }

    private long secondsUntil(Instant end) {
        Duration remaining = Duration.between(clock.instant(), end);
        if (remaining.isNegative() || remaining.isZero()) {
            return 0;
        }
        return (remaining.toMillis() + 999) / 1000;
    }

    public record AttemptResult(int attemptsRemaining, long retryAfterSeconds) {

        public boolean locked() {
            return retryAfterSeconds > 0;
        }
    }

    private record AttemptState(int failures, Instant lockedUntil) {
    }
}
