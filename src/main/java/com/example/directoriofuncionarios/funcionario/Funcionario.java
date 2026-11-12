package com.example.directoriofuncionarios.funcionario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String cargo;

    @Column(nullable = false)
    private String departamento;

    private String telefono;

    protected Funcionario() {
    }

    public Funcionario(String nombre, String cargo, String departamento, String telefono) {
        this.nombre = nombre;
        this.cargo = cargo;
        this.departamento = departamento;
        this.telefono = telefono;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCargo() {
        return cargo;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void actualizar(String nombre, String cargo, String departamento, String telefono) {
        this.nombre = nombre;
        this.cargo = cargo;
        this.departamento = departamento;
        this.telefono = telefono;
    }
}
