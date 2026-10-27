package es.ejemplo.gestion.dao;

import es.ejemplo.gestion.model.Producto;
import es.ejemplo.gestion.model.Vehiculo;

import java.sql.*;
import java.util.*;

public class VehiculoDao extends AbstractDao<Vehiculo> {
    public VehiculoDao(String url) {
        super(url);
    }

    @Override
    protected Optional<Vehiculo> mapRsToClass(ResultSet rs) {
        try {
            return Optional.of(new Vehiculo(rs.getString("matricula"), rs.getString("marca"), rs.getString("modelo"), rs.getInt("anio")));
        } catch (SQLException | RuntimeException e) {
            registrar("mapRsToClass vehiculo", e);
            return Optional.empty();
        }
    }

    public List<Vehiculo> listar() {
        // TODO: seguir el ejemplo de ProductoDao.listar()
        String sql = "SELECT matricula, marca, modelo, anio FROM Vehiculos ORDER BY matricula ASC";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar vehículos", e);
            return List.of();
        }
    }

    public Optional<Vehiculo> buscarPorId(String matricula) {
        // TODO: SELECT WHERE matricula = ?; ps.setString(1, matricula)
        String sql = "SELECT matricula, marca, modelo, anio FROM Vehiculos WHERE matricula = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return consultarUno(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return Optional.empty();
        }
    }

    public Optional<Vehiculo> buscarPorMatricula(String matricula) {
        // TODO: delegar en buscarPorId(matricula)
        return buscarPorId(matricula);
    }

    public boolean eliminar(String matricula) {
        // TODO: DELETE WHERE matricula = ?
        String sql = "DELETE FROM Vehiculos WHERE matricula = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, matricula);
            return ejecutar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return false;
        }
    }

    public boolean insertar(Vehiculo v) {
        // TODO: validar e insertar
        String sql = "INSERT INTO Vehiculos (matricula, marca, modelo, anio) VALUES (?, ?, ?, ?)";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getMatricula());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getAnio());
            return ejecutar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return false;
        }
    }

    public boolean actualizar(Vehiculo v) {
        String sql = "UPDATE Vehiculos SET marca = ?, modelo = ?, anio = ? WHERE matricula = ?";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(4, v.getMatricula());
            ps.setString(1, v.getMarca());
            ps.setString(2, v.getModelo());
            ps.setInt(3, v.getAnio());
            return ejecutar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return false;
        }
    }

    public List<Vehiculo> buscarPorMarca(String texto) {
        String sql = "SELECT matricula, marca, modelo, anio FROM Vehiculos WHERE LOWER(marca) LIKE LOWER(?) ORDER BY matricula ASC";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + (texto == null ? "" : texto) + "%");
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar vehículos", e);
            return List.of();
        }
    }

    public List<Vehiculo> buscarPorAnio(int desde, int hasta) {
        // TODO: BETWEEN inclusivo
        String sql = "SELECT matricula, marca, modelo, anio FROM Vehiculos WHERE anio >= ? AND anio <= ? ORDER BY anio ASC, matricula ASC";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, desde);
            ps.setInt(2, hasta);
            return consultar(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar vehículos", e);
            return List.of();
        }
    }

    public int contar() {
        // TODO: consultarEntero(ps)
        String sql = "SELECT COUNT(*) FROM Vehiculos";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultarEntero(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return 0;
        }
    }

    public Map<String, Integer> contarPorMarca() {
        // TODO: GROUP BY marca y consultarAgrupado(ps)
        String sql = "SELECT marca, COUNT(*) AS total FROM Vehiculos GROUP BY marca ORDER BY total DESC, marca ASC";
        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            return consultarAgrupado(ps);
        } catch (SQLException | RuntimeException e) {
            registrar("listar productos", e);
            return Map.of();
        }
    }
}