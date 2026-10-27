package es.ejemplo.taller.dao;

import es.ejemplo.taller.infra.ConexionBD;
import es.ejemplo.taller.modelo.Mecanico;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MecanicoDao {

    private final String url;

    public MecanicoDao(String url) {
        this.url = url;
    }

    /**
     * Mapea una fila del ResultSet a un objeto Mecanico.
     */
    private Mecanico mapear(ResultSet rs) throws SQLException {
        return new Mecanico(
                rs.getInt("id_mecanico"),
                rs.getString("nombre"),
                rs.getString("especialidad"),
                rs.getBigDecimal("tarifa_hora")
        );
    }

    /**
     * Lista todos los mecanicos registrados.
     */
    public List<Mecanico> listar() {
        List<Mecanico> resultado = new ArrayList<>();
        String sql = "SELECT id_mecanico, nombre, especialidad, tarifa_hora FROM mecanicos";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar mecanicos: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Busca un mecanico por su identificador.
     */
    public Optional<Mecanico> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id_mecanico, nombre, especialidad, tarifa_hora FROM mecanicos WHERE id_mecanico = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar mecanico por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Inserta un nuevo mecanico con id autogenerado.
     */
    public int insertar(Mecanico dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getNombre() == null || dato.getNombre().isBlank()) {
            return 0;
        }
        if (dato.getEspecialidad() == null || dato.getEspecialidad().isBlank()) {
            return 0;
        }
        if (dato.getTarifaHora() == null || dato.getTarifaHora().compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }

        String sql = "INSERT INTO mecanicos (nombre, especialidad, tarifa_hora) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dato.getNombre().trim());
            ps.setString(2, dato.getEspecialidad().trim());
            ps.setBigDecimal(3, dato.getTarifaHora());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar mecanico: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Actualiza los datos de un mecanico existente.
     */
    public int actualizar(Mecanico dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getIdMecanico() == null) {
            return 0;
        }
        if (dato.getNombre() == null || dato.getNombre().isBlank()) {
            return 0;
        }
        if (dato.getEspecialidad() == null || dato.getEspecialidad().isBlank()) {
            return 0;
        }
        if (dato.getTarifaHora() == null || dato.getTarifaHora().compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }

        String sql = "UPDATE mecanicos SET nombre = ?, especialidad = ?, tarifa_hora = ? WHERE id_mecanico = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dato.getNombre().trim());
            ps.setString(2, dato.getEspecialidad().trim());
            ps.setBigDecimal(3, dato.getTarifaHora());
            ps.setInt(4, dato.getIdMecanico());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar mecanico: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Elimina un mecanico por su identificador.
     */
    public int eliminar(Integer id) {
        if (id == null) {
            return 0;
        }
        String sql = "DELETE FROM mecanicos WHERE id_mecanico = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar mecanico: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Busca mecanicos por coincidencia exacta de especialidad.
     */
    public List<Mecanico> buscarPorEspecialidad(String especialidad) {
        if (especialidad == null || especialidad.isBlank()) {
            return List.of();
        }
        List<Mecanico> resultado = new ArrayList<>();
        String sql = "SELECT id_mecanico, nombre, especialidad, tarifa_hora FROM mecanicos WHERE especialidad = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, especialidad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar mecanicos por especialidad: " + e.getMessage());
        }
        return resultado;
    }
}
