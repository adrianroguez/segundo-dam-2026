package es.ejemplo.taller.dao;

import es.ejemplo.taller.modelo.Cliente;

import java.sql.*;
import java.util.*;
import java.math.BigDecimal;

public class ClienteDao {
    private final String url;

    public ClienteDao(String url) {
        this.url = url;
    }

    public List<Cliente> listar() {
        // TODO: implementar según el contrato del README.
        return List.of();
    }

    public Optional<Cliente> buscarPorId(Integer id) {
        // TODO: implementar según el contrato del README.
        return Optional.empty();
    }

    public int insertar(Cliente dato) {
        // TODO: implementar según el contrato del README.
        return 0;
    }

    public int actualizar(Cliente dato) {
        // TODO: implementar según el contrato del README.
        return 0;
    }

    public int eliminar(Integer id) {
        // TODO: implementar según el contrato del README.
        return 0;
    }
}