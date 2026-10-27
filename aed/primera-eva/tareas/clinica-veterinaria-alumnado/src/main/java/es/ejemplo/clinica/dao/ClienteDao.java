package es.ejemplo.clinica.dao;

import es.ejemplo.clinica.infra.ConexionBD;
import es.ejemplo.clinica.modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ClienteDao implements IClienteDao {
    private final String url;

    /**
     * Constructor del DAO con la URL de la base de datos.
     * @param url direccion JDBC de la base de datos
     */
    public ClienteDao(String url) {
        this.url = url;
    }

    /**
     * Mapea el registro actual del resultado a un objeto Cliente.
     * @param rs conjunto de resultados posicionado en la fila actual
     * @return instancia de Cliente
     * @throws SQLException si ocurre un error al leer las columnas
     */
    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("email"),
                rs.getString("telefono")
        );
    }

    /**
     * Obtiene la lista completa de clientes ordenados por identificador.
     * @return lista de clientes
     */
    @Override
    public List<Cliente> listar() {
        String sql = "SELECT id, nombre, email, telefono FROM clientes ORDER BY id ASC";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
            return clientes;
        } catch (SQLException e) {
            throw new DaoException("Error al listar clientes", e);
        }
    }

    /**
     * Busca un cliente por su identificador unico.
     * @param id identificador del cliente
     * @return Optional con el cliente encontrado o vacio si no existe
     */
    @Override
    public Optional<Cliente> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        String sql = "SELECT id, nombre, email, telefono FROM clientes WHERE id = ?";
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
            throw new DaoException("Error al buscar cliente por id", e);
        }
    }

    /**
     * Inserta un nuevo cliente y retorna el identificador generado.
     * @param entidad cliente a insertar
     * @return identificador generado en la base de datos
     */
    @Override
    public int insertar(Cliente entidad) {
        if (entidad == null) {
            throw new IllegalArgumentException("La entidad cliente no puede ser nula");
        }
        String sql = "INSERT INTO clientes (nombre, email, telefono) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEmail());
            ps.setString(3, entidad.getTelefono());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    entidad.setId(idGenerado);
                    return idGenerado;
                }
                throw new DaoException("No se obtuvo identificador generado para cliente", null);
            }
        } catch (SQLException e) {
            throw new DaoException("Error al insertar cliente", e);
        }
    }

    /**
     * Actualiza los datos de un cliente existente.
     * @param entidad cliente con datos actualizados
     * @return numero de filas afectadas
     */
    @Override
    public int actualizar(Cliente entidad) {
        if (entidad == null || entidad.getId() == null) {
            throw new IllegalArgumentException("El cliente o su identificador no pueden ser nulos");
        }
        String sql = "UPDATE clientes SET nombre = ?, email = ?, telefono = ? WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, entidad.getNombre());
            ps.setString(2, entidad.getEmail());
            ps.setString(3, entidad.getTelefono());
            ps.setInt(4, entidad.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al actualizar cliente", e);
        }
    }

    /**
     * Elimina un cliente por su identificador.
     * @param id identificador del cliente a eliminar
     * @return numero de filas afectadas
     */
    @Override
    public int eliminar(Integer id) {
        Objects.requireNonNull(id, "El identificador no puede ser nulo");
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection con = ConexionBD.abrir(url);
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Error al eliminar cliente", e);
        }
    }
}
