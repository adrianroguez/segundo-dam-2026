package es.ejemplo.biblioteca.dao;

import java.sql.*;
import java.util.*;

import es.ejemplo.biblioteca.infr.ConexionBD;
import es.ejemplo.biblioteca.modelo.*;

/**
 * Implementación pendiente: el alumnado debe completar los métodos JDBC.
 */
public final class SocioDao implements ISocioDao {
    private final String url;

    public SocioDao(String url) {
        this.url = Objects.requireNonNull(url);
    }

    private Socio mapRsToClass(ResultSet rs) throws SQLException {
        return new Socio(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("email"),
                rs.getString("fecha_alta")
        );
    }

    @Override
    public List<Socio> listar() {
        String sql = "SELECT id, nombre, email, fecha_alta FROM socios ORDER BY id";
        List<Socio> list = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRsToClass(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Error al listar", e);
        }
    }

    @Override
    public Optional<Socio> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, nombre, email, fecha_alta FROM socios WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return Optional.of(mapRsToClass(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al buscar por id", e);
        }
    }

    @Override
    public int insertar(Socio entidad) {
        if (entidad == null) throw new IllegalArgumentException("La entidad no puede ser nula");
        String sql = "INSERT INTO socios (nombre, email, fecha_alta) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEmail());
            ps.setString(3, entidad.getFechaAlta());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    entidad.setId(idGenerado);
                    return idGenerado;
                }
            }
            throw new DaoException("No se obtuvo identificador generado", null);
        } catch (SQLException e) {
            throw new DaoException("Error al insertar", e);
        }
    }

    @Override
    public int actualizar(Socio entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("El socio o su identificador no pueden ser nulos");
        }
        String sql = "UPDATE socios SET nombre = ?, email = ?, fecha_alta = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEmail());
            ps.setString(3, entidad.getFechaAlta());
            ps.setInt(4, entidad.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar", e);
        }
    }

    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El identificador no puede ser nulo");
        String sql = "DELETE FROM socios WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar", e);
        }
    }

    @Override
    public Optional<Socio> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        String sql = "SELECT id, nombre, email, fecha_alta FROM socios WHERE email = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    return Optional.of(mapRsToClass(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al buscar por email", e);
        }
    }

    @Override
    public List<Socio> listarAltasDesde(String fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        String sql = "SELECT id, nombre, email, fecha_alta FROM socios WHERE fecha_alta >= ?";
        List<Socio> list = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, fecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRsToClass(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Error al listar", e);
        }
    }
}
