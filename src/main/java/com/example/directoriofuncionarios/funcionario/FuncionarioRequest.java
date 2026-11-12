package com.example.directoriofuncionarios.funcionario;

import jakarta.validation.constraints.NotBlank;

public record FuncionarioRequest(
        Long id,
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,
        @NotBlank(message = "El cargo es obligatorio")
        String cargo,
        @NotBlank(message = "El departamento es obligatorio")
        String departamento,
        String telefono) {
}
