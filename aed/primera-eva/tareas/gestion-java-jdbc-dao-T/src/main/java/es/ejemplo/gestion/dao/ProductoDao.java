package es.ejemplo.gestion.dao;

import es.ejemplo.gestion.model.Producto;

import java.sql.*;
import java.util.*;

public class ProductoDao extends AbstractDao<Producto> {
    public ProductoDao(String url) {
        super(url);
    }

    /**
     * EJEMPLO COMPLETO: transformar ResultSet a Producto sin propagar excepciones.
     */
    @Override
    protected Optional<Producto> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Producto(rs.getInt("id"), rs.getString("nombre"), rs.getDouble("precio")));
        } catch (SQLException | RuntimeException e) {
            registrar("mapRsToClass producto", e);
            return Optional.empty();
        }
    }

    /**
     * EJEMPLO COMPLETO: listar productos mediante PreparedStatement.
     */
    public List<Producto> listar() {
        String sql = "SELECT id,nombre,precio FROM Productos ORDER BY id";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return List.of();
        }
    }

    // TODO: implementa cada método con PreparedStatement y try-with-resources.
    // IMPORTANTE: buscarPorId y eliminar usan Integer; la clase base NO necesita ID.
    public Optional<Producto> buscarPorId(Integer id) {
        // TODO: SELECT ... WHERE id = ?; ps.setInt(1, id); consultarUno(ps)
        String sql = "SELECT id, nombre, precio FROM Productos WHERE id = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return consultarUno(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return Optional.empty();
        }
    }

    public boolean eliminar(Integer id) {
        String sql = "DELETE FROM Productos WHERE id = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ejecutar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return false;
        }
    }

    public boolean insertar(Producto p) {
        String sql = "INSERT INTO Productos (id, nombre, precio) VALUES (?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre());
            ps.setDouble(3, p.getPrecio());
            return ejecutar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return false;
        }
    }

    public boolean actualizar(Producto p) {
        String sql = "UPDATE Productos SET nombre = ?, precio = ? WHERE id = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getId());
            return ejecutar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return false;
        }
    }

    public List<Producto> buscarPorNombre(String texto) {
        String sql = "SELECT id, nombre, precio FROM Productos WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre COLLATE NOCASE ASC, id ASC";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (texto == null ? "" : texto) + "%");
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return List.of();
        }
    }

    public List<Producto> buscarPorPrecio(double minimo, double maximo) {
        String sql = "SELECT id, nombre, precio FROM Productos WHERE precio >= ? AND precio <= ? ORDER BY precio ASC, id ASC";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, minimo);
            ps.setDouble(2, maximo);
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return List.of();
        }
    }

    public int contar() {
        String sql = "SELECT COUNT(*) FROM Productos";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultarEntero(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return 0;
        }
    }

    public double precioMedio() {
        String sql = "SELECT COALESCE(AVG(precio), 0.0) FROM Productos";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultarDecimal(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return 0;
        }
    }
}