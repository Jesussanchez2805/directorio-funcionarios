package com.example.directoriofuncionarios.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    @PostMapping("/validar")
    public ResponseEntity<Void> validarCredenciales() {
        return ResponseEntity.noContent().build();
    }
}
