package com.example.directoriofuncionarios.auth;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BruteForceAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;

    public BruteForceAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            LoginAttemptService loginAttemptService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName().trim();

        UserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException exception) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        long lockRemaining = loginAttemptService.remainingLockSeconds(username);
        if (lockRemaining > 0) {
            throw locked(lockRemaining);
        }

        String password = authentication.getCredentials() == null
                ? ""
                : authentication.getCredentials().toString();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            LoginAttemptService.AttemptResult result = loginAttemptService.recordFailure(username);
            if (result.locked()) {
                throw locked(result.retryAfterSeconds());
            }
            throw new LoginAuthenticationException(
                    "Credenciales incorrectas. Te quedan " + result.attemptsRemaining() + " intentos.",
                    result.attemptsRemaining(),
                    0);
        }

        if (!user.isEnabled() || !user.isAccountNonLocked()
                || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
            throw new LockedException("La cuenta no está disponible");
        }

        loginAttemptService.reset(username);
        return UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private LoginAuthenticationException locked(long retryAfterSeconds) {
        return new LoginAuthenticationException(
                "Se agotaron los 3 intentos. Espera " + retryAfterSeconds
                        + " segundos antes de volver a intentarlo.",
                0,
                retryAfterSeconds);
    }
}
