package com.example.directoriofuncionarios.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class LoginAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public LoginAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        if (exception instanceof LoginAuthenticationException loginException) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("error", loginException.getMessage());
            if (loginException.getRetryAfterSeconds() > 0) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", Long.toString(loginException.getRetryAfterSeconds()));
                body.put("segundosEspera", loginException.getRetryAfterSeconds());
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                body.put("intentosRestantes", loginException.getAttemptsRemaining());
            }
            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        objectMapper.writeValue(response.getOutputStream(), Map.of("error", "Usuario o contraseña incorrectos."));
    }
}
