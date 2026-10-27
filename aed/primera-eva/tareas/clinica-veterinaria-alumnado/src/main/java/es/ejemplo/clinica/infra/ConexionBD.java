package es.ejemplo.clinica.infra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ConexionBD {
    private String url;

    /**
     * Valida la URL de conexion y la existencia del archivo de base de datos.
     * @param url direccion JDBC de la base de datos
     * @throws SQLException si la URL o el archivo no son validos
     */
    public ConexionBD(String url) throws SQLException {
        if (url == null || !url.startsWith("jdbc:sqlite:")) {
            throw new SQLException("URL no valida o protocolo no soportado: " + url);
        }
        String nuevaURL = url.substring("jdbc:sqlite:".length());
        this.url = url;
        if (!":memory:".equals(nuevaURL) && !nuevaURL.startsWith("file:")) {
            Path path = Path.of(nuevaURL);

            if (!Files.exists(path)) {
                throw new SQLException("No existe la base de datos");
            }

            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                throw new SQLException("La base de datos no es leible o editable");
            }
        }
    }

    /**
     * Devuelve la URL de la base de datos configurada.
     * @return URL de conexion
     */
    public String getUrl() {
        return url;
    }

    /**
     * Verifica el archivo antes de conectar y comprueba las tablas del esquema.
     * Devuelve una conexion abierta con claves foraneas activadas.
     * @param url direccion JDBC de la base de datos
     * @return conexion activa a la base de datos
     * @throws SQLException si la ruta el archivo o el esquema no son validos
     */
    public static Connection abrir(String url) throws SQLException {
        new ConexionBD(url);
        Connection connection = DriverManager.getConnection(url);

        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException cierre) {
                e.addSuppressed(cierre);
            }
            throw e;
        }

        String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' "
                + "AND name IN ('clientes','mascotas','veterinarios','consultas','tratamientos')";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next() || rs.getInt(1) != 5) {
                throw new SQLException("El esquema de la base de datos esta incompleto");
            }
            return connection;
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException cierre) {
                e.addSuppressed(cierre);
            }
            throw e;
        }
    }
}

