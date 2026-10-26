package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Consulta;

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

public final class ConsultaDao implements IConsultaDao {
    private final String url;

    /**
     * Constructor del DAO con la URL de la base de datos.
     * @param url direccion JDBC de la base de datos
     */
    public ConsultaDao(String url) {
        this.url = url;
    }

    /**
     * Mapea el registro actual del resultado a un objeto Consulta.
     * @param rs conjunto de resultados posicionado en la fila actual
     * @return instancia de Consulta
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private Consulta mapear(ResultSet rs) throws SQLException {
        BigDecimal horas = rs.getBigDecimal("horas");
        return new Consulta(
                rs.getInt("id"),
                rs.getInt("id_mascota"),
                rs.getInt("id_veterinario"),
                rs.getString("fecha"),
                horas,
                rs.getString("estado")
        );
    }

    /**
     * Obtiene la lista completa de consultas ordenadas por identificador.
     * @return lista de consultas
     */
    @Override
    public List<Consulta> listar() {
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas ORDER BY id ASC";
        List<Consulta> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al listar consultas", e);
        }
    }

    /**
     * Busca una consulta por su identificador unico.
     * @param id identificador de la consulta
     * @return Optional con la consulta encontrada o vacio si no existe
     */
    @Override
    public Optional<Consulta> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas WHERE id = ?";
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
            throw new DaoException("Error al buscar consulta por id", e);
        }
    }

    /**
     * Inserta una nueva consulta y retorna el identificador generado.
     * @param entidad consulta a insertar
     * @return identificador generado en la base de datos
     */
    @Override
    public int insertar(Consulta entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad consulta no puede ser nula");
        }
        String sql = "INSERT INTO consultas (id_mascota, id_veterinario, fecha, horas, estado) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (entidad.getIdMascota() != null) {
                ps.setInt(1, entidad.getIdMascota());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            if (entidad.getIdVeterinario() != null) {
                ps.setInt(2, entidad.getIdVeterinario());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, entidad.getFecha());
            ps.setBigDecimal(4, entidad.getHoras());
            ps.setString(5, entidad.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    entidad.setId(idGenerado);
                    return idGenerado;
                }
                throw new DaoException("No se obtuvo identificador generado para consulta", null);
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar consulta", e);
        }
    }

    /**
     * Actualiza los datos de una consulta existente.
     * @param entidad consulta con datos actualizados
     * @return numero de filas afectadas
     */
    @Override
    public int actualizar(Consulta entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("La consulta o su identificador no pueden ser nulos");
        }
        String sql = "UPDATE consultas SET id_mascota = ?, id_veterinario = ?, fecha = ?, horas = ?, estado = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (entidad.getIdMascota() != null) {
                ps.setInt(1, entidad.getIdMascota());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            if (entidad.getIdVeterinario() != null) {
                ps.setInt(2, entidad.getIdVeterinario());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, entidad.getFecha());
            ps.setBigDecimal(4, entidad.getHoras());
            ps.setString(5, entidad.getEstado());
            ps.setInt(6, entidad.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar consulta", e);
        }
    }

    /**
     * Elimina una consulta por su identificador.
     * @param id identificador de la consulta a eliminar
     * @return numero de filas afectadas
     */
    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El identificador no puede ser nulo");
        String sql = "DELETE FROM consultas WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar consulta", e);
        }
    }

    /**
     * Busca las consultas asociadas a una mascota especifica.
     * @param idMascota identificador de la mascota
     * @return lista de consultas de la mascota
     */
    @Override
    public List<Consulta> buscarPorMascota(Integer idMascota) {
        if (idMascota == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas WHERE id_mascota = ? ORDER BY id ASC";
        List<Consulta> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMascota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al buscar consultas por mascota", e);
        }
    }

    /**
     * Busca las consultas atendidas por un veterinario especifico.
     * @param idVeterinario identificador del veterinario
     * @return lista de consultas atendidas por el veterinario
     */
    @Override
    public List<Consulta> buscarPorVeterinario(Integer idVeterinario) {
        if (idVeterinario == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas WHERE id_veterinario = ? ORDER BY id ASC";
        List<Consulta> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idVeterinario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al buscar consultas por veterinario", e);
        }
    }

    /**
     * Lista las consultas en estado programada ordenadas por identificador.
     * @return lista de consultas programadas
     */
    @Override
    public List<Consulta> listarProgramadas() {
        String sql = "SELECT id, id_mascota, id_veterinario, fecha, horas, estado FROM consultas WHERE estado = 'PROGRAMADA' ORDER BY id ASC";
        List<Consulta> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al listar consultas programadas", e);
        }
    }
}
