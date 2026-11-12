package com.example.directoriofuncionarios.funcionario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import com.example.directoriofuncionarios.auth.LoginAuthenticationEntryPoint;
import com.example.directoriofuncionarios.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(FuncionarioController.class)
@Import({ApiExceptionHandler.class, SecurityConfig.class, LoginAuthenticationEntryPoint.class})
@TestPropertySource(properties = {"app.security.username=test-admin", "app.security.password=test-secret"})
class FuncionarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FuncionarioService service;

    @Test
    void listarDevuelve204CuandoNoHayFuncionarios() throws Exception {
        when(service.listar(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/funcionarios").with(httpBasic("test-admin", "test-secret")))
                .andExpect(status().isNoContent());
    }

    @Test
    void crearDevuelve201YLaUbicacionDelFuncionario() throws Exception {
        Funcionario funcionario = new Funcionario("Ana Pérez", "Analista", "Contabilidad", null);
        ReflectionTestUtils.setField(funcionario, "id", 1L);
        when(service.crear(any())).thenReturn(funcionario);

        mockMvc.perform(post("/api/v1/funcionarios")
                        .with(httpBasic("test-admin", "test-secret"))
                        .contentType("application/json")
                        .content("""
                                {"nombre":"Ana Pérez","cargo":"Analista","departamento":"Contabilidad"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/funcionarios/1"))
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"));
    }

    @Test
    void crearRechazaCamposObligatoriosEnBlanco() throws Exception {
        mockMvc.perform(post("/api/v1/funcionarios")
                        .with(httpBasic("test-admin", "test-secret"))
                        .contentType("application/json")
                        .content("""
                                {"nombre":"  ","cargo":"Analista","departamento":"Contabilidad"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El nombre es obligatorio"));
    }

    @Test
    void crearRechazaUnIdNoNulo() throws Exception {
        when(service.crear(any())).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID no se puede especificar al crear"));

        mockMvc.perform(post("/api/v1/funcionarios")
                        .with(httpBasic("test-admin", "test-secret"))
                        .contentType("application/json")
                        .content("""
                                {"id":5,"nombre":"Ana Pérez","cargo":"Analista","departamento":"Contabilidad"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El ID no se puede especificar al crear"));
    }

    @Test
    void filtrosSeEnrutanAlServicio() throws Exception {
        when(service.listar("ana", "Contabilidad")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/funcionarios")
                        .with(httpBasic("test-admin", "test-secret"))
                        .param("nombre", "ana")
                        .param("departamento", "Contabilidad"))
                .andExpect(status().isNoContent());

        verify(service).listar("ana", "Contabilidad");
    }
}
