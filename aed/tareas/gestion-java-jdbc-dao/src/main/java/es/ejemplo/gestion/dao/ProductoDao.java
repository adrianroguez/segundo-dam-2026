package es.ejemplo.gestion.dao;

import es.ejemplo.gestion.config.ConexionBD;
import es.ejemplo.gestion.exception.AplicationException;
import es.ejemplo.gestion.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ProductoDao {
    private final ConexionBD conexionBD;

    public ProductoDao(String url) {
        this.conexionBD = new ConexionBD(url);
    }

    public List<Producto> listar() {
        String sql = "SELECT id, nombre, precio FROM Productos ORDER BY id ASC";
        List<Producto> productos = new ArrayList<>();
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(new Producto(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getDouble("precio")
                ));
            }
            return productos;
        } catch (SQLException e) {
            throw new AplicationException("Error al listar los productos", e);
        }
    }

    public Optional<Producto> buscarPorId(int id) {
        String sql = "SELECT id, nombre, precio FROM Productos WHERE id = ?";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio")
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AplicationException("Error al buscar el producto por ID", e);
        }
    }

    public boolean insertar(Producto producto) {
        if (producto == null) {
            throw new AplicationException("El producto no puede ser nulo");
        }
        String sql = "INSERT INTO Productos (id, nombre, precio) VALUES (?, ?, ?)";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, producto.getId());
            ps.setString(2, producto.getNombre());
            ps.setDouble(3, producto.getPrecio());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AplicationException("Error al insertar el producto: " + e.getMessage(), e);
        }
    }

    public boolean actualizar(Producto producto) {
        if (producto == null) {
            throw new AplicationException("El producto no puede ser nulo");
        }
        String sql = "UPDATE Productos SET nombre = ?, precio = ? WHERE id = ?";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AplicationException("Error al actualizar el producto: " + e.getMessage(), e);
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM Productos WHERE id = ?";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AplicationException("Error al eliminar el producto: " + e.getMessage(), e);
        }
    }

    public List<Producto> buscarPorNombre(String texto) {
        String sql = "SELECT id, nombre, precio FROM Productos WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre COLLATE NOCASE ASC, id ASC";
        List<Producto> productos = new ArrayList<>();
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (texto == null ? "" : texto) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio")
                    ));
                }
                return productos;
            }
        } catch (SQLException e) {
            throw new AplicationException("Error al buscar productos por nombre", e);
        }
    }

    public List<Producto> buscarPorPrecio(double minimo, double maximo) {
        if (minimo > maximo) {
            return new ArrayList<>();
        }
        String sql = "SELECT id, nombre, precio FROM Productos WHERE precio >= ? AND precio <= ? ORDER BY precio ASC, id ASC";
        List<Producto> productos = new ArrayList<>();
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, minimo);
            ps.setDouble(2, maximo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productos.add(new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio")
                    ));
                }
                return productos;
            }
        } catch (SQLException e) {
            throw new AplicationException("Error al buscar productos por rango de precio", e);
        }
    }

    public int contar() {
        String sql = "SELECT COUNT(*) FROM Productos";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new AplicationException("Error al contar los productos", e);
        }
    }

    public double precioMedio() {
        String sql = "SELECT COALESCE(AVG(precio), 0.0) FROM Productos";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } catch (SQLException e) {
            throw new AplicationException("Error al calcular el precio medio", e);
        }
    }
}