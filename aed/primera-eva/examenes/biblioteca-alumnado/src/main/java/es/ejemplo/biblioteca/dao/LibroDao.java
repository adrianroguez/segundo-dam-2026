package es.ejemplo.biblioteca.dao;

import java.sql.*;
import java.util.*;

import es.ejemplo.biblioteca.infr.ConexionBD;
import es.ejemplo.biblioteca.modelo.*;

public final class LibroDao implements ILibroDao {
    private final String url;

    public LibroDao(String url) {
        this.url = Objects.requireNonNull(url);
    }

    private Libro mapRsToClass(ResultSet rs) throws SQLException {
        return new Libro(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("autor"),
                rs.getInt("anio_publicacion")
        );
    }

    @Override
    public List<Libro> listar() {
        String sql = "SELECT id, titulo, autor, anio_publicacion FROM libros ORDER BY id";
        List<Libro> list = new ArrayList<>();
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
    public Optional<Libro> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, titulo, autor, anio_publicacion FROM libros WHERE id = ?";
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
    public int insertar(Libro entidad) {
        if (entidad == null)
            throw new IllegalArgumentException("La entidad no puede ser nula");
        String sql = "INSERT INTO libros (titulo, autor, anio_publicacion) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getTitulo());
            ps.setString(2, entidad.getAutor());
            ps.setInt(3, entidad.getAnioPublicacion());
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
    public int actualizar(Libro entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("El libro o su identificador no pueden ser nulos");
        }
        String sql = "UPDATE libros SET titulo = ?, autor = ?, anio_publicacion = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getTitulo());
            ps.setString(2, entidad.getAutor());
            ps.setInt(3, entidad.getAnioPublicacion());
            ps.setInt(4, entidad.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar", e);
        }
    }

    @Override
    public int eliminar(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("El identificador no puede ser nulo");
        }
        String sql = "DELETE FROM libros WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar", e);
        }
    }
}
