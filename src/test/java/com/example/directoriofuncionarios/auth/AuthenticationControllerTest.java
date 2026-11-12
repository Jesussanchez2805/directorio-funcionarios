package com.example.directoriofuncionarios.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.directoriofuncionarios.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"app.security.username=test-admin", "app.security.password=test-secret"})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }

    @Test
    void exigeAutenticacionParaValidar() throws Exception {
        mockMvc.perform(post("/api/v1/auth/validar"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"));
    }
}
