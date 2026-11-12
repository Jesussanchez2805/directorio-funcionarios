package com.example.directoriofuncionarios.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    @Test
    void liberaElUsuarioAlCumplirseLosTreintaSegundos() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T12:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(clock);

        assertEquals(2, service.recordFailure("admin").attemptsRemaining());
        assertEquals(1, service.recordFailure("admin").attemptsRemaining());
        LoginAttemptService.AttemptResult blocked = service.recordFailure("admin");
        assertEquals(30, blocked.retryAfterSeconds());
        assertEquals(30, service.remainingLockSeconds("admin"));

        clock.advanceSeconds(29);
        assertEquals(1, service.remainingLockSeconds("admin"));

        clock.advanceSeconds(1);
        assertEquals(0, service.remainingLockSeconds("admin"));
        assertEquals(2, service.recordFailure("admin").attemptsRemaining());
    }

    @Test
    void reiniciaElContadorCuandoElUsuarioSeAutentica() {
        LoginAttemptService service = new LoginAttemptService(Clock.systemUTC());
        service.recordFailure("admin");
        service.recordFailure("admin");

        service.reset("admin");

        assertEquals(2, service.recordFailure("admin").attemptsRemaining());
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advanceSeconds(long seconds) {
            instant = instant.plusSeconds(seconds);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
