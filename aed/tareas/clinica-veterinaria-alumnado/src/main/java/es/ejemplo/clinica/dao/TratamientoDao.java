package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Tratamiento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class TratamientoDao implements ITratamientoDao {
    private final String url;

    /**
     * Constructor del DAO con la URL de la base de datos.
     * @param url direccion JDBC de la base de datos
     */
    public TratamientoDao(String url) {
        this.url = url;
    }

    /**
     * Mapea el registro actual del resultado a un objeto Tratamiento.
     * @param rs conjunto de resultados posicionado en la fila actual
     * @return instancia de Tratamiento
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private Tratamiento mapear(ResultSet rs) throws SQLException {
        BigDecimal precio = rs.getBigDecimal("precio_unitario");
        return new Tratamiento(
                rs.getInt("id"),
                rs.getInt("id_consulta"),
                rs.getString("descripcion"),
                rs.getInt("cantidad"),
                precio
        );
    }

    /**
     * Obtiene la lista completa de tratamientos ordenados por identificador.
     * @return lista de tratamientos
     */
    @Override
    public List<Tratamiento> listar() {
        String sql = "SELECT id, id_consulta, descripcion, cantidad, precio_unitario FROM tratamientos ORDER BY id ASC";
        List<Tratamiento> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al listar tratamientos", e);
        }
    }

    /**
     * Busca un tratamiento por su identificador unico.
     * @param id identificador del tratamiento
     * @return Optional con el tratamiento encontrado o vacio si no existe
     */
    @Override
    public Optional<Tratamiento> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, id_consulta, descripcion, cantidad, precio_unitario FROM tratamientos WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Error al buscar tratamiento por id", e);
        }
    }

    /**
     * Inserta un nuevo tratamiento y retorna el identificador generado.
     * @param entidad tratamiento a insertar
     * @return identificador generado en la base de datos
     */
    @Override
    public int insertar(Tratamiento entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad tratamiento no puede ser nula");
        }
        String sql = "INSERT INTO tratamientos (id_consulta, descripcion, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (entidad.getIdConsulta() != null) {
                ps.setInt(1, entidad.getIdConsulta());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, entidad.getDescripcion());
            if (entidad.getCantidad() != null) {
                ps.setInt(3, entidad.getCantidad());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setBigDecimal(4, entidad.getPrecioUnitario());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    entidad.setId(idGenerado);
                    return idGenerado;
                }
                throw new DaoException("No se obtuvo identificador generado para tratamiento", null);
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar tratamiento", e);
        }
    }

    /**
     * Actualiza los datos de un tratamiento existente.
     * @param entidad tratamiento con datos actualizados
     * @return numero de filas afectadas
     */
    @Override
    public int actualizar(Tratamiento entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("El tratamiento o su identificador no pueden ser nulos");
        }
        String sql = "UPDATE tratamientos SET id_consulta = ?, descripcion = ?, cantidad = ?, precio_unitario = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (entidad.getIdConsulta() != null) {
                ps.setInt(1, entidad.getIdConsulta());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, entidad.getDescripcion());
            if (entidad.getCantidad() != null) {
                ps.setInt(3, entidad.getCantidad());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setBigDecimal(4, entidad.getPrecioUnitario());
            ps.setInt(5, entidad.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar tratamiento", e);
        }
    }

    /**
     * Elimina un tratamiento por su identificador.
     * @param id identificador del tratamiento a eliminar
     * @return numero de filas afectadas
     */
    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El identificador no puede ser nulo");
        String sql = "DELETE FROM tratamientos WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar tratamiento", e);
        }
    }

    /**
     * Lista los tratamientos aplicados a una consulta especifica.
     * @param idConsulta identificador de la consulta
     * @return lista de tratamientos asociados a la consulta
     */
    @Override
    public List<Tratamiento> listarPorConsulta(Integer idConsulta) {
        if (idConsulta == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT id, id_consulta, descripcion, cantidad, precio_unitario FROM tratamientos WHERE id_consulta = ? ORDER BY id ASC";
        List<Tratamiento> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idConsulta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al listar tratamientos por consulta", e);
        }
    }

    /**
     * Calcula el coste total de los tratamientos asociados a una consulta.
     * @param idConsulta identificador de la consulta
     * @return importe total de los tratamientos o cero si no tiene tratamientos
     */
    @Override
    public BigDecimal calcularCosteTratamientos(Integer idConsulta) {
        if (idConsulta == null) {
            return BigDecimal.ZERO;
        }
        String sql = "SELECT cantidad, precio_unitario FROM tratamientos WHERE id_consulta = ?";
        BigDecimal total = BigDecimal.ZERO;
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idConsulta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int cantidad = rs.getInt("cantidad");
                    BigDecimal precio = rs.getBigDecimal("precio_unitario");
                    if (precio != null) {
                        total = total.add(precio.multiply(BigDecimal.valueOf(cantidad)));
                    }
                }
            }
            return total;
        } catch (SQLException e) {
            throw new DaoException("Error al calcular coste de tratamientos", e);
        }
    }
}
