package com.example.directoriofuncionarios.auth;

import org.springframework.security.core.AuthenticationException;

public class LoginAuthenticationException extends AuthenticationException {

    private final int attemptsRemaining;
    private final long retryAfterSeconds;

    public LoginAuthenticationException(String message, int attemptsRemaining, long retryAfterSeconds) {
        super(message);
        this.attemptsRemaining = attemptsRemaining;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
