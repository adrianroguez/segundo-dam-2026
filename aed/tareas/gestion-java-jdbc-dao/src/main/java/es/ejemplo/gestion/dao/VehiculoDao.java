package es.ejemplo.gestion.dao;

import es.ejemplo.gestion.config.ConexionBD;
import es.ejemplo.gestion.exception.AplicationException;
import es.ejemplo.gestion.model.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class VehiculoDao {
    private final ConexionBD conexionBD;

    public VehiculoDao(String url) {
        this.conexionBD = new ConexionBD(url);
    }

    public List<Vehiculo> listar() {
        String sql = "SELECT matricula, marca, modelo, anio FROM Vehiculos ORDER BY matricula ASC";
        List<Vehiculo> vehiculos = new ArrayList<>();
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vehiculos.add(new Vehiculo(
                    rs.getString("matricula"),
                    rs.getString("marca"),
                    rs.getString("modelo"),
                    rs.getInt("anio")
                ));
            }
            return vehiculos;
        } catch (SQLException e) {
            throw new AplicationException("Error al listar los vehículos", e);
        }
    }

    public Optional<Vehiculo> buscarPorMatricula(String matricula) {
        String sql = "SELECT matricula, marca, modelo, anio FROM Vehiculos WHERE matricula = ?";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Vehiculo(
                        rs.getString("matricula"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new AplicationException("Error al buscar el vehículo por matrícula", e);
        }
    }

    public boolean insertar(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new AplicationException("El vehículo no puede ser nulo");
        }
        String sql = "INSERT INTO Vehiculos (matricula, marca, modelo, anio) VALUES (?, ?, ?, ?)";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getMatricula());
            ps.setString(2, vehiculo.getMarca());
            ps.setString(3, vehiculo.getModelo());
            ps.setInt(4, vehiculo.getAnio());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AplicationException("Error al insertar el vehículo: " + e.getMessage(), e);
        }
    }

    public boolean actualizar(Vehiculo vehiculo) {
        if (vehiculo == null) {
            throw new AplicationException("El vehículo no puede ser nulo");
        }
        String sql = "UPDATE Vehiculos SET marca = ?, modelo = ?, anio = ? WHERE matricula = ?";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, vehiculo.getMarca());
            ps.setString(2, vehiculo.getModelo());
            ps.setInt(3, vehiculo.getAnio());
            ps.setString(4, vehiculo.getMatricula());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AplicationException("Error al actualizar el vehículo: " + e.getMessage(), e);
        }
    }

    public boolean eliminar(String matricula) {
        String sql = "DELETE FROM Vehiculos WHERE matricula = ?";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AplicationException("Error al eliminar el vehículo: " + e.getMessage(), e);
        }
    }

    public List<Vehiculo> buscarPorMarca(String texto) {
        String sql = "SELECT matricula, marca, modelo, anio FROM Vehiculos WHERE LOWER(marca) LIKE LOWER(?) ORDER BY matricula ASC";
        List<Vehiculo> vehiculos = new ArrayList<>();
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (texto == null ? "" : texto) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehiculos.add(new Vehiculo(
                        rs.getString("matricula"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    ));
                }
                return vehiculos;
            }
        } catch (SQLException e) {
            throw new AplicationException("Error al buscar vehículos por marca", e);
        }
    }

    public List<Vehiculo> buscarPorAnio(int desde, int hasta) {
        if (desde > hasta) {
            return new ArrayList<>();
        }
        String sql = "SELECT matricula, marca, modelo, anio FROM Vehiculos WHERE anio >= ? AND anio <= ? ORDER BY anio ASC, matricula ASC";
        List<Vehiculo> vehiculos = new ArrayList<>();
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, desde);
            ps.setInt(2, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vehiculos.add(new Vehiculo(
                        rs.getString("matricula"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("anio")
                    ));
                }
                return vehiculos;
            }
        } catch (SQLException e) {
            throw new AplicationException("Error al buscar vehículos por rango de año", e);
        }
    }

    public int contar() {
        String sql = "SELECT COUNT(*) FROM Vehiculos";
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new AplicationException("Error al contar los vehículos", e);
        }
    }

    public Map<String, Integer> contarPorMarca() {
        String sql = "SELECT marca, COUNT(*) AS total FROM Vehiculos GROUP BY marca ORDER BY total DESC, marca ASC";
        Map<String, Integer> resultado = new LinkedHashMap<>();
        try (Connection con = conexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.put(rs.getString("marca"), rs.getInt("total"));
            }
            return resultado;
        } catch (SQLException e) {
            throw new AplicationException("Error al contar vehículos por marca", e);
        }
    }
}