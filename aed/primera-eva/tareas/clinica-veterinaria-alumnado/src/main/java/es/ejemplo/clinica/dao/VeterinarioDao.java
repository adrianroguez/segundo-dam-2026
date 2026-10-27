package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Veterinario;

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

public final class VeterinarioDao implements IVeterinarioDao {
    private final String url;

    /**
     * Constructor del DAO con la URL de la base de datos.
     * @param url direccion JDBC de la base de datos
     */
    public VeterinarioDao(String url) {
        this.url = url;
    }

    /**
     * Mapea el registro actual del resultado a un objeto Veterinario.
     * @param rs conjunto de resultados posicionado en la fila actual
     * @return instancia de Veterinario
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private Veterinario mapear(ResultSet rs) throws SQLException {
        BigDecimal tarifa = rs.getBigDecimal("tarifa_hora");
        return new Veterinario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("especialidad"),
                tarifa
        );
    }

    /**
     * Obtiene la lista completa de veterinarios ordenados por identificador.
     * @return lista de veterinarios
     */
    @Override
    public List<Veterinario> listar() {
        String sql = "SELECT id, nombre, especialidad, tarifa_hora FROM veterinarios ORDER BY id ASC";
        List<Veterinario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al listar veterinarios", e);
        }
    }

    /**
     * Busca un veterinario por su identificador unico.
     * @param id identificador del veterinario
     * @return Optional con el veterinario encontrado o vacio si no existe
     */
    @Override
    public Optional<Veterinario> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, nombre, especialidad, tarifa_hora FROM veterinarios WHERE id = ?";
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
            throw new DaoException("Error al buscar veterinario por id", e);
        }
    }

    /**
     * Inserta un nuevo veterinario y retorna el identificador generado.
     * @param entidad veterinario a insertar
     * @return identificador generado en la base de datos
     */
    @Override
    public int insertar(Veterinario entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad veterinario no puede ser nula");
        }
        String sql = "INSERT INTO veterinarios (nombre, especialidad, tarifa_hora) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEspecialidad());
            ps.setBigDecimal(3, entidad.getTarifaHora());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    entidad.setId(idGenerado);
                    return idGenerado;
                }
                throw new DaoException("No se obtuvo identificador generado para veterinario", null);
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar veterinario", e);
        }
    }

    /**
     * Actualiza los datos de un veterinario existente.
     * @param entidad veterinario con datos actualizados
     * @return numero de filas afectadas
     */
    @Override
    public int actualizar(Veterinario entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("El veterinario o su identificador no pueden ser nulos");
        }
        String sql = "UPDATE veterinarios SET nombre = ?, especialidad = ?, tarifa_hora = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEspecialidad());
            ps.setBigDecimal(3, entidad.getTarifaHora());
            ps.setInt(4, entidad.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar veterinario", e);
        }
    }

    /**
     * Elimina un veterinario por su identificador.
     * @param id identificador del veterinario a eliminar
     * @return numero de filas afectadas
     */
    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El identificador no puede ser nulo");
        String sql = "DELETE FROM veterinarios WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar veterinario", e);
        }
    }

    /**
     * Busca veterinarios por su especialidad ordenados por tarifa horaria ascendente.
     * @param especialidad especialidad a consultar
     * @return lista de veterinarios coincidentes
     */
    @Override
    public List<Veterinario> buscarPorEspecialidad(String especialidad) {
        String sql = "SELECT id, nombre, especialidad, tarifa_hora FROM veterinarios WHERE especialidad = ? ORDER BY tarifa_hora ASC, id ASC";
        List<Veterinario> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, especialidad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al buscar veterinarios por especialidad", e);
        }
    }
}
