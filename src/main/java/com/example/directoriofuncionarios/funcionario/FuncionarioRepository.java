package com.example.directoriofuncionarios.funcionario;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    List<Funcionario> findByNombreContainingIgnoreCase(String nombre);

    List<Funcionario> findByDepartamentoIgnoreCase(String departamento);
}
