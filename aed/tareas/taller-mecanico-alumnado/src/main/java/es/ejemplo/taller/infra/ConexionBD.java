package es.ejemplo.taller.infra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ConexionBD {
    private String url;

    public ConexionBD(String url) throws SQLException {
        if (url == null || !url.startsWith("jdbc:sqlite:")) {
            throw new SQLException("URL no válida o protocolo no soportado: " + url);
        }
        String nuevaURL = url.substring("jdbc:sqlite:".length());
        this.url = url;
        Path path = Path.of(nuevaURL);

        if (!Files.exists(path)) {
            throw new SQLException("No existe la base de datos");
        }

        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new SQLException("La base de datos no es leible o editable");
        }
    }

    public String getUrl() {
        return url;
    }

    /**
     * Verifica el archivo antes de conectar y comprobar las tablas.
     * Devuelve una conexión abierta cuya responsabilidad de cierre tiene el
     * llamador.
     * Si la ruta, el archivo o el esquema no son válidos, lanza SQLException.
     */
    public static Connection abrir(String url) throws SQLException {
        new ConexionBD(url);
        Connection connection = DriverManager.getConnection(url);

        String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' "
                + "AND name IN ('clientes','vehiculos','mecanicos','reparaciones','piezas_reparacion')";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next() || rs.getInt(1) != 5) {
                throw new SQLException("El esquema de la base de datos está incompleto");
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
