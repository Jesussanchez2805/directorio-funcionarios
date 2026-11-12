package com.example.directoriofuncionarios.funcionario;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository repository;

    private FuncionarioService service;

    @BeforeEach
    void setUp() {
        service = new FuncionarioService(repository);
    }

    @Test
    void priorizaNombreYRecortaLosFiltros() {
        List<Funcionario> expected = List.of();
        when(repository.findByNombreContainingIgnoreCase("ana")).thenReturn(expected);

        assertSame(expected, service.listar(" ana ", " Contabilidad "));

        verify(repository).findByNombreContainingIgnoreCase("ana");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void trataLosFiltrosEnBlancoComoAusentes() {
        List<Funcionario> expected = List.of();
        when(repository.findAll()).thenReturn(expected);

        assertSame(expected, service.listar("  ", " \t"));

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void filtraDepartamentoCompletoSinExigirMayusculas() {
        List<Funcionario> expected = List.of();
        when(repository.findByDepartamentoIgnoreCase("Contabilidad")).thenReturn(expected);

        assertSame(expected, service.listar(null, " Contabilidad "));

        verify(repository).findByDepartamentoIgnoreCase("Contabilidad");
        verifyNoMoreInteractions(repository);
    }
}
