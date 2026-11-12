package com.example.directoriofuncionarios.funcionario;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class FuncionarioService {

    private final FuncionarioRepository repository;

    public FuncionarioService(FuncionarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listar(String nombre, String departamento) {
        String nombreFiltro = limpiarFiltro(nombre);
        if (nombreFiltro != null) {
            return repository.findByNombreContainingIgnoreCase(nombreFiltro);
        }

        String departamentoFiltro = limpiarFiltro(departamento);
        if (departamentoFiltro != null) {
            return repository.findByDepartamentoIgnoreCase(departamentoFiltro);
        }

        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Funcionario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Funcionario no encontrado"));
    }

    public Funcionario crear(FuncionarioRequest request) {
        if (request.id() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID no se puede especificar al crear");
        }
        return repository.save(new Funcionario(
                request.nombre().trim(),
                request.cargo().trim(),
                request.departamento().trim(),
                limpiarTelefono(request.telefono())));
    }

    public Funcionario actualizar(Long id, FuncionarioRequest request) {
        Funcionario funcionario = buscarPorId(id);
        funcionario.actualizar(
                request.nombre().trim(),
                request.cargo().trim(),
                request.departamento().trim(),
                limpiarTelefono(request.telefono()));
        return repository.save(funcionario);
    }

    public void eliminar(Long id) {
        Funcionario funcionario = buscarPorId(id);
        repository.delete(funcionario);
    }

    private String limpiarFiltro(String filtro) {
        return filtro == null || filtro.isBlank() ? null : filtro.trim();
    }

    private String limpiarTelefono(String telefono) {
        return telefono == null || telefono.isBlank() ? null : telefono.trim();
    }
}
