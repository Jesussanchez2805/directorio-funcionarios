package com.example.directoriofuncionarios.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.directoriofuncionarios.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, LoginAuthenticationEntryPoint.class})
@TestPropertySource(properties = {"app.security.username=test-admin", "app.security.password=test-secret"})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void resetLoginAttempts() {
        loginAttemptService.reset("test-admin");
    }

    @Test
    void validaLasCredencialesYResponde204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validar")
                        .with(httpBasic("test-admin", "test-secret")))
                .andExpect(status().isNoContent());
    }

    @Test
    void rechazaCredencialesIncorrectasSinLanzarDialogoBasic() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validar")
                        .with(httpBasic("test-admin", "incorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.intentosRestantes").value(2));
    }

    @Test
    void bloqueaPor30SegundosAlTercerIntentoIncorrecto() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/v1/auth/validar")
                            .with(httpBasic("test-admin", "incorrecta")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/validar")
                        .with(httpBasic("test-admin", "incorrecta")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "30"))
                .andExpect(jsonPath("$.segundosEspera").value(30));

        mockMvc.perform(post("/api/v1/auth/validar")
                        .with(httpBasic("test-admin", "test-secret")))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void credencialesCorrectasRestablecenLosIntentosFallidos() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validar")
                        .with(httpBasic("test-admin", "incorrecta")))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/validar")
                        .with(httpBasic("test-admin", "test-secret")))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/validar")
                        .with(httpBasic("test-admin", "incorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.intentosRestantes").value(2));
    }

    @Test
    void usuarioDesconocidoNoBloqueaElUsuarioValido() throws Exception {
        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/auth/validar")
                            .with(httpBasic("usuario-inexistente", "incorrecta")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/auth/validar")
                        .with(httpBasic("test-admin", "test-secret")))
                .andExpect(status().isNoContent());
    }

    @Test
    void exigeAutenticacionParaValidar() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validar"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }
}
