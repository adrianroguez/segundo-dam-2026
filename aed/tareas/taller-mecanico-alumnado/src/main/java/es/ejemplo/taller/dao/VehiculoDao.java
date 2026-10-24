package es.ejemplo.taller.dao;

import es.ejemplo.taller.infra.ConexionBD;
import es.ejemplo.taller.modelo.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VehiculoDao {

    private final String url;

    public VehiculoDao(String url) {
        this.url = url;
    }

    /**
     * Mapea una fila del ResultSet a un objeto Vehiculo.
     */
    private Vehiculo mapear(ResultSet rs) throws SQLException {
        return new Vehiculo(
                rs.getString("matricula"),
                rs.getString("marca"),
                rs.getString("modelo"),
                rs.getInt("anio"),
                rs.getInt("id_cliente")
        );
    }

    /**
     * Lista todos los vehiculos registrados.
     */
    public List<Vehiculo> listar() {
        List<Vehiculo> resultado = new ArrayList<>();
        String sql = "SELECT matricula, marca, modelo, anio, id_cliente FROM vehiculos";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar vehiculos: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Busca un vehiculo por su matricula.
     */
    public Optional<Vehiculo> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        String sql = "SELECT matricula, marca, modelo, anio, id_cliente FROM vehiculos WHERE matricula = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar vehiculo por matricula: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Inserta un nuevo vehiculo con clave provista por el objeto.
     */
    public int insertar(Vehiculo dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getMatricula() == null || dato.getMatricula().isBlank()) {
            return 0;
        }
        if (dato.getMarca() == null || dato.getMarca().isBlank()) {
            return 0;
        }
        if (dato.getModelo() == null || dato.getModelo().isBlank()) {
            return 0;
        }
        if (dato.getAnio() == null) {
            return 0;
        }
        if (dato.getIdCliente() == null) {
            return 0;
        }

        String sql = "INSERT INTO vehiculos (matricula, marca, modelo, anio, id_cliente) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dato.getMatricula().trim());
            ps.setString(2, dato.getMarca().trim());
            ps.setString(3, dato.getModelo().trim());
            ps.setInt(4, dato.getAnio());
            ps.setInt(5, dato.getIdCliente());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al insertar vehiculo: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Actualiza los datos de un vehiculo existente.
     */
    public int actualizar(Vehiculo dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getMatricula() == null || dato.getMatricula().isBlank()) {
            return 0;
        }
        if (dato.getMarca() == null || dato.getMarca().isBlank()) {
            return 0;
        }
        if (dato.getModelo() == null || dato.getModelo().isBlank()) {
            return 0;
        }
        if (dato.getAnio() == null) {
            return 0;
        }
        if (dato.getIdCliente() == null) {
            return 0;
        }

        String sql = "UPDATE vehiculos SET marca = ?, modelo = ?, anio = ?, id_cliente = ? WHERE matricula = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dato.getMarca().trim());
            ps.setString(2, dato.getModelo().trim());
            ps.setInt(3, dato.getAnio());
            ps.setInt(4, dato.getIdCliente());
            ps.setString(5, dato.getMatricula().trim());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar vehiculo: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Elimina un vehiculo por su matricula.
     */
    public int eliminar(String id) {
        if (id == null || id.isBlank()) {
            return 0;
        }
        String sql = "DELETE FROM vehiculos WHERE matricula = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.trim());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar vehiculo: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Busca vehiculos asociados a un cliente especifico.
     */
    public List<Vehiculo> buscarPorCliente(Integer idCliente) {
        if (idCliente == null) {
            return List.of();
        }
        List<Vehiculo> resultado = new ArrayList<>();
        String sql = "SELECT matricula, marca, modelo, anio, id_cliente FROM vehiculos WHERE id_cliente = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar vehiculos por cliente: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Lista vehiculos filtrados por coincidencia exacta de marca.
     */
    public List<Vehiculo> listarPorMarca(String marca) {
        if (marca == null || marca.isBlank()) {
            return List.of();
        }
        List<Vehiculo> resultado = new ArrayList<>();
        String sql = "SELECT matricula, marca, modelo, anio, id_cliente FROM vehiculos WHERE marca = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, marca);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar vehiculos por marca: " + e.getMessage());
        }
        return resultado;
    }
}
