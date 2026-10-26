package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Mascota;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class MascotaDao implements IMascotaDao {
    private final String url;

    /**
     * Constructor del DAO con la URL de la base de datos.
     * @param url direccion JDBC de la base de datos
     */
    public MascotaDao(String url) {
        this.url = url;
    }

    /**
     * Mapea el registro actual del resultado a un objeto Mascota.
     * @param rs conjunto de resultados posicionado en la fila actual
     * @return instancia de Mascota
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private Mascota mapear(ResultSet rs) throws SQLException {
        return new Mascota(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("especie"),
                rs.getString("raza"),
                rs.getString("fecha_nacimiento"),
                rs.getInt("id_cliente")
        );
    }

    /**
     * Obtiene la lista completa de mascotas ordenadas por identificador.
     * @return lista de mascotas
     */
    @Override
    public List<Mascota> listar() {
        String sql = "SELECT id, nombre, especie, raza, fecha_nacimiento, id_cliente FROM mascotas ORDER BY id ASC";
        List<Mascota> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al listar mascotas", e);
        }
    }

    /**
     * Busca una mascota por su identificador unico.
     * @param id identificador de la mascota
     * @return Optional con la mascota encontrada o vacio si no existe
     */
    @Override
    public Optional<Mascota> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, nombre, especie, raza, fecha_nacimiento, id_cliente FROM mascotas WHERE id = ?";
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
            throw new DaoException("Error al buscar mascota por id", e);
        }
    }

    /**
     * Inserta una nueva mascota y retorna el identificador generado.
     * @param entidad mascota a insertar
     * @return identificador generado en la base de datos
     */
    @Override
    public int insertar(Mascota entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad mascota no puede ser nula");
        }
        String sql = "INSERT INTO mascotas (nombre, especie, raza, fecha_nacimiento, id_cliente) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEspecie());
            ps.setString(3, entidad.getRaza());
            ps.setString(4, entidad.getFechaNacimiento());
            if (entidad.getIdCliente() != null) {
                ps.setInt(5, entidad.getIdCliente());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    entidad.setId(idGenerado);
                    return idGenerado;
                }
                throw new DaoException("No se obtuvo identificador generado para mascota", null);
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar mascota", e);
        }
    }

    /**
     * Actualiza los datos de una mascota existente.
     * @param entidad mascota con datos actualizados
     * @return numero de filas afectadas
     */
    @Override
    public int actualizar(Mascota entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("La mascota o su identificador no pueden ser nulos");
        }
        String sql = "UPDATE mascotas SET nombre = ?, especie = ?, raza = ?, fecha_nacimiento = ?, id_cliente = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEspecie());
            ps.setString(3, entidad.getRaza());
            ps.setString(4, entidad.getFechaNacimiento());
            if (entidad.getIdCliente() != null) {
                ps.setInt(5, entidad.getIdCliente());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setInt(6, entidad.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar mascota", e);
        }
    }

    /**
     * Elimina una mascota por su identificador.
     * @param id identificador de la mascota a eliminar
     * @return numero de filas afectadas
     */
    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El identificador no puede ser nulo");
        String sql = "DELETE FROM mascotas WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar mascota", e);
        }
    }

    /**
     * Busca las mascotas asociadas a un cliente especifico.
     * @param idCliente identificador del cliente propietario
     * @return lista de mascotas del cliente
     */
    @Override
    public List<Mascota> buscarPorCliente(Integer idCliente) {
        if (idCliente == null) {
            return new ArrayList<>();
        }
        String sql = "SELECT id, nombre, especie, raza, fecha_nacimiento, id_cliente FROM mascotas WHERE id_cliente = ? ORDER BY id ASC";
        List<Mascota> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al buscar mascotas por cliente", e);
        }
    }

    /**
     * Lista las mascotas pertenecientes a una especie determinada.
     * @param especie especie de animal a filtrar
     * @return lista de mascotas de dicha especie
     */
    @Override
    public List<Mascota> listarPorEspecie(String especie) {
        String sql = "SELECT id, nombre, especie, raza, fecha_nacimiento, id_cliente FROM mascotas WHERE especie = ? ORDER BY id ASC";
        List<Mascota> lista = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, especie);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new DaoException("Error al listar mascotas por especie", e);
        }
    }
}
