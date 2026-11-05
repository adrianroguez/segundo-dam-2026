package es.ejemplo.biblioteca.dao;

import java.sql.*;
import java.util.*;

import es.ejemplo.biblioteca.infr.ConexionBD;
import es.ejemplo.biblioteca.modelo.*;

/**
 * Implementación pendiente: el alumnado debe completar los métodos JDBC.
 */
public final class PrestamoDao implements IPrestamoDao {
    private final String url;

    public PrestamoDao(String url) {
        this.url = Objects.requireNonNull(url);
    }

    private Prestamo mapRsToClass(ResultSet rs) throws SQLException {
        return new Prestamo(
                rs.getInt("id"),
                rs.getInt("id_libro"),
                rs.getInt("id_socio"),
                rs.getString("fecha_prestamo"),
                rs.getString("fecha_devolucion")
        );
    }

    @Override
    public List<Prestamo> listar() {
        String sql = "SELECT id, id_libro, id_socio, fecha_prestamo, fecha_devolucion FROM prestamos ORDER BY id";
        List<Prestamo> list = new ArrayList<>();
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
    public Optional<Prestamo> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, id_libro, id_socio, fecha_prestamo, fecha_devolucion FROM prestamos WHERE id = ?";
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
    public int insertar(Prestamo entidad) {
        if (entidad == null)
            throw new IllegalArgumentException("La entidad no puede ser nula");
        String sql = "INSERT INTO prestamos (id_libro, id_socio, fecha_prestamo, fecha_devolucion) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entidad.getIdLibro());
            ps.setInt(2, entidad.getIdSocio());
            ps.setString(3, entidad.getFechaPrestamo());
            ps.setString(4, entidad.getFechaDevolucion());
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
    public int actualizar(Prestamo entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("El socio o su identificador no pueden ser nulos");
        }
        String sql = "UPDATE prestamos SET id_libro = ?, id_socio = ?, fecha_prestamo = ?, fecha_devolucion = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, entidad.getIdLibro());
            ps.setInt(2, entidad.getIdSocio());
            ps.setString(3, entidad.getFechaPrestamo());
            ps.setString(4, entidad.getFechaDevolucion());
            ps.setInt(5, entidad.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar", e);
        }
    }

    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El identificador no puede ser nulo");
        String sql = "DELETE FROM prestamos WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar", e);
        }
    }

    @Override
    public List<PrestamosPorSocio> contarPrestamosPorSocio() {
        String sql = "SELECT s.id AS id_socio, s.nombre, COUNT(p.id) AS total_prestamos " +
                     "FROM socios s " +
                     "LEFT JOIN prestamos p ON s.id = p.id_socio " +
                     "GROUP BY s.id, s.nombre " +
                     "ORDER BY total_prestamos DESC";
        List<PrestamosPorSocio> list = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PrestamosPorSocio(
                        rs.getInt("id_socio"),
                        rs.getString("nombre"),
                        rs.getLong("total_prestamos")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Error al contar préstamos por socio", e);
        }
    }

    @Override
    public List<Prestamo> listarPrestamosPorAutor(String autor) {
        if (autor == null) {
            return Collections.emptyList();
        }
        String sql = "SELECT id, id_libro, id_socio, fecha_prestamo, fecha_devolucion " +
                     "FROM prestamos " +
                     "WHERE id_libro IN (SELECT id FROM libros WHERE autor = ?) " +
                     "ORDER BY id";
        List<Prestamo> list = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, autor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRsToClass(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException("Error al listar préstamos por autor", e);
        }
    }
}
