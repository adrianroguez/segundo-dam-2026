package es.ejemplo.taller.dao;

import es.ejemplo.taller.infra.ConexionBD;
import es.ejemplo.taller.modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteDao {

    private final String url;

    public ClienteDao(String url) {
        this.url = url;
    }

    /**
     * Mapea una fila del ResultSet a un objeto Cliente.
     */
    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_cliente"),
                rs.getString("nombre"),
                rs.getString("email"),
                rs.getString("telefono")
        );
    }

    /**
     * Lista todos los clientes registrados.
     */
    public List<Cliente> listar() {
        List<Cliente> resultado = new ArrayList<>();
        String sql = "SELECT id_cliente, nombre, email, telefono FROM clientes";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar clientes: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Busca un cliente por su identificador.
     */
    public Optional<Cliente> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id_cliente, nombre, email, telefono FROM clientes WHERE id_cliente = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar cliente por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Inserta un nuevo cliente con id autogenerado.
     */
    public int insertar(Cliente dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getNombre() == null || dato.getNombre().isBlank()) {
            return 0;
        }
        if (dato.getEmail() == null || dato.getEmail().isBlank()) {
            return 0;
        }

        String sql = "INSERT INTO clientes (nombre, email, telefono) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dato.getNombre().trim());
            ps.setString(2, dato.getEmail().trim());
            ps.setString(3, dato.getTelefono());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar cliente: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Actualiza los datos de un cliente existente.
     */
    public int actualizar(Cliente dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getIdCliente() == null) {
            return 0;
        }
        if (dato.getNombre() == null || dato.getNombre().isBlank()) {
            return 0;
        }
        if (dato.getEmail() == null || dato.getEmail().isBlank()) {
            return 0;
        }

        String sql = "UPDATE clientes SET nombre = ?, email = ?, telefono = ? WHERE id_cliente = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dato.getNombre().trim());
            ps.setString(2, dato.getEmail().trim());
            ps.setString(3, dato.getTelefono());
            ps.setInt(4, dato.getIdCliente());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Elimina un cliente por su identificador.
     */
    public int eliminar(Integer id) {
        if (id == null) {
            return 0;
        }
        String sql = "DELETE FROM clientes WHERE id_cliente = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return 0;
        }
    }
}
