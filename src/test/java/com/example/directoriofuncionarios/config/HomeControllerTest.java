package com.example.directoriofuncionarios.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"app.security.username=test-admin", "app.security.password=test-secret"})
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void paginaPrincipalRedirigeAlInicioDeSesion() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html"));
    }
}
