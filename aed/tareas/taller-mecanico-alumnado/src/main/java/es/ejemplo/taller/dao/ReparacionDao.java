package es.ejemplo.taller.dao;

import es.ejemplo.taller.infra.ConexionBD;
import es.ejemplo.taller.modelo.Reparacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReparacionDao {

    private final String url;

    public ReparacionDao(String url) {
        this.url = url;
    }

    /**
     * Mapea una fila del ResultSet a un objeto Reparacion.
     */
    private Reparacion mapear(ResultSet rs) throws SQLException {
        return new Reparacion(
                rs.getInt("id_reparacion"),
                rs.getString("matricula"),
                rs.getInt("id_mecanico"),
                rs.getString("descripcion"),
                rs.getString("fecha"),
                rs.getBigDecimal("horas_trabajo"),
                rs.getString("estado")
        );
    }

    /**
     * Lista todas las reparaciones registradas.
     */
    public List<Reparacion> listar() {
        List<Reparacion> resultado = new ArrayList<>();
        String sql = "SELECT id_reparacion, matricula, id_mecanico, descripcion, fecha, horas_trabajo, estado FROM reparaciones";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar reparaciones: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Busca una reparacion por su identificador.
     */
    public Optional<Reparacion> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id_reparacion, matricula, id_mecanico, descripcion, fecha, horas_trabajo, estado FROM reparaciones WHERE id_reparacion = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar reparacion por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Inserta una nueva reparacion con id autogenerado.
     */
    public int insertar(Reparacion dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getMatricula() == null || dato.getMatricula().isBlank()) {
            return 0;
        }
        if (dato.getIdMecanico() == null) {
            return 0;
        }
        if (dato.getDescripcion() == null || dato.getDescripcion().isBlank()) {
            return 0;
        }
        if (dato.getFecha() == null || dato.getFecha().isBlank()) {
            return 0;
        }
        if (dato.getHorasTrabajo() == null || dato.getHorasTrabajo().compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }
        if (dato.getEstado() == null || dato.getEstado().isBlank()) {
            return 0;
        }

        String sql = "INSERT INTO reparaciones (matricula, id_mecanico, descripcion, fecha, horas_trabajo, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dato.getMatricula().trim());
            ps.setInt(2, dato.getIdMecanico());
            ps.setString(3, dato.getDescripcion().trim());
            ps.setString(4, dato.getFecha().trim());
            ps.setBigDecimal(5, dato.getHorasTrabajo());
            ps.setString(6, dato.getEstado());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar reparacion: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Actualiza los datos de una reparacion existente.
     */
    public int actualizar(Reparacion dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getIdReparacion() == null) {
            return 0;
        }
        if (dato.getMatricula() == null || dato.getMatricula().isBlank()) {
            return 0;
        }
        if (dato.getIdMecanico() == null) {
            return 0;
        }
        if (dato.getDescripcion() == null || dato.getDescripcion().isBlank()) {
            return 0;
        }
        if (dato.getFecha() == null || dato.getFecha().isBlank()) {
            return 0;
        }
        if (dato.getHorasTrabajo() == null || dato.getHorasTrabajo().compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }
        if (dato.getEstado() == null || dato.getEstado().isBlank()) {
            return 0;
        }

        String sql = "UPDATE reparaciones SET matricula = ?, id_mecanico = ?, descripcion = ?, fecha = ?, " +
                "horas_trabajo = ?, estado = ? WHERE id_reparacion = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dato.getMatricula().trim());
            ps.setInt(2, dato.getIdMecanico());
            ps.setString(3, dato.getDescripcion().trim());
            ps.setString(4, dato.getFecha().trim());
            ps.setBigDecimal(5, dato.getHorasTrabajo());
            ps.setString(6, dato.getEstado());
            ps.setInt(7, dato.getIdReparacion());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar reparacion: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Elimina una reparacion por su identificador.
     */
    public int eliminar(Integer id) {
        if (id == null) {
            return 0;
        }
        String sql = "DELETE FROM reparaciones WHERE id_reparacion = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar reparacion: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Busca reparaciones asociadas a un vehiculo por matricula.
     */
    public List<Reparacion> buscarPorVehiculo(String valor) {
        if (valor == null || valor.isBlank()) {
            return List.of();
        }
        List<Reparacion> resultado = new ArrayList<>();
        String sql = "SELECT id_reparacion, matricula, id_mecanico, descripcion, fecha, horas_trabajo, estado " +
                "FROM reparaciones WHERE matricula = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, valor.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar reparaciones por vehiculo: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Busca reparaciones asignadas a un mecanico por su id.
     */
    public List<Reparacion> buscarPorMecanico(Integer valor) {
        if (valor == null) {
            return List.of();
        }
        List<Reparacion> resultado = new ArrayList<>();
        String sql = "SELECT id_reparacion, matricula, id_mecanico, descripcion, fecha, horas_trabajo, estado " +
                "FROM reparaciones WHERE id_mecanico = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar reparaciones por mecanico: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Lista todas las reparaciones con estado PENDIENTE.
     */
    public List<Reparacion> listarPendientes() {
        List<Reparacion> resultado = new ArrayList<>();
        String sql = "SELECT id_reparacion, matricula, id_mecanico, descripcion, fecha, horas_trabajo, estado " +
                "FROM reparaciones WHERE estado = 'PENDIENTE'";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar reparaciones pendientes: " + e.getMessage());
        }
        return resultado;
    }
}
