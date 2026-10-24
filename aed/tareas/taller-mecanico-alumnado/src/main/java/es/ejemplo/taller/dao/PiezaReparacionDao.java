package es.ejemplo.taller.dao;

import es.ejemplo.taller.infra.ConexionBD;
import es.ejemplo.taller.modelo.PiezaReparacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PiezaReparacionDao {

    private final String url;

    public PiezaReparacionDao(String url) {
        this.url = url;
    }

    /**
     * Mapea una fila del ResultSet a un objeto PiezaReparacion.
     */
    private PiezaReparacion mapear(ResultSet rs) throws SQLException {
        return new PiezaReparacion(
                rs.getInt("id_pieza"),
                rs.getInt("id_reparacion"),
                rs.getString("nombre"),
                rs.getInt("cantidad"),
                rs.getBigDecimal("precio_unitario")
        );
    }

    /**
     * Lista todas las piezas registradas.
     */
    public List<PiezaReparacion> listar() {
        List<PiezaReparacion> resultado = new ArrayList<>();
        String sql = "SELECT id_pieza, id_reparacion, nombre, cantidad, precio_unitario FROM piezas_reparacion";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar piezas: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Busca una pieza por su identificador.
     */
    public Optional<PiezaReparacion> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id_pieza, id_reparacion, nombre, cantidad, precio_unitario FROM piezas_reparacion WHERE id_pieza = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar pieza por id: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Inserta una nueva pieza con id autogenerado.
     */
    public int insertar(PiezaReparacion dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getIdReparacion() == null) {
            return 0;
        }
        if (dato.getNombre() == null || dato.getNombre().isBlank()) {
            return 0;
        }
        if (dato.getCantidad() == null || dato.getCantidad() <= 0) {
            return 0;
        }
        if (dato.getPrecioUnitario() == null || dato.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }

        String sql = "INSERT INTO piezas_reparacion (id_reparacion, nombre, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dato.getIdReparacion());
            ps.setString(2, dato.getNombre().trim());
            ps.setInt(3, dato.getCantidad());
            ps.setBigDecimal(4, dato.getPrecioUnitario());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar pieza: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Actualiza los datos de una pieza existente.
     */
    public int actualizar(PiezaReparacion dato) {
        if (dato == null) {
            return 0;
        }
        if (dato.getIdPieza() == null) {
            return 0;
        }
        if (dato.getIdReparacion() == null) {
            return 0;
        }
        if (dato.getNombre() == null || dato.getNombre().isBlank()) {
            return 0;
        }
        if (dato.getCantidad() == null || dato.getCantidad() <= 0) {
            return 0;
        }
        if (dato.getPrecioUnitario() == null || dato.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
            return 0;
        }

        String sql = "UPDATE piezas_reparacion SET id_reparacion = ?, nombre = ?, cantidad = ?, precio_unitario = ? WHERE id_pieza = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dato.getIdReparacion());
            ps.setString(2, dato.getNombre().trim());
            ps.setInt(3, dato.getCantidad());
            ps.setBigDecimal(4, dato.getPrecioUnitario());
            ps.setInt(5, dato.getIdPieza());
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar pieza: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Elimina una pieza por su identificador.
     */
    public int eliminar(Integer id) {
        if (id == null) {
            return 0;
        }
        String sql = "DELETE FROM piezas_reparacion WHERE id_pieza = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al eliminar pieza: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Lista piezas asociadas a una reparacion.
     */
    public List<PiezaReparacion> listarPorReparacion(Integer id) {
        if (id == null) {
            return List.of();
        }
        List<PiezaReparacion> resultado = new ArrayList<>();
        String sql = "SELECT id_pieza, id_reparacion, nombre, cantidad, precio_unitario FROM piezas_reparacion WHERE id_reparacion = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar piezas por reparacion: " + e.getMessage());
        }
        return resultado;
    }

    /**
     * Calcula el coste total de piezas para una reparacion.
     */
    public BigDecimal calcularCostePiezas(Integer id) {
        if (id == null) {
            return BigDecimal.ZERO;
        }
        String sql = "SELECT COALESCE(SUM(cantidad * precio_unitario), 0) FROM piezas_reparacion WHERE id_reparacion = ?";
        try (Connection conn = ConexionBD.abrir(this.url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal coste = rs.getBigDecimal(1);
                    return coste != null ? coste : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular coste de piezas para reparacion: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
