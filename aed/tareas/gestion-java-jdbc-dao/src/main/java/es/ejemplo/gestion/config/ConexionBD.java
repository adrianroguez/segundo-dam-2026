package es.ejemplo.gestion.config;

import es.ejemplo.gestion.exception.AplicationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Abre una conexion en la base SQLite indicada por el constructor. */
public class ConexionBD {
    private final String url;

    public ConexionBD(String url) {
        this.url = url;
    }

    public Connection obtenerConexion() {
        if (url == null || url.isBlank()) {
            throw new AplicationException("La URL de conexión no puede ser nula ni vacía.");
        }
        if (url.startsWith("jdbc:sqlite:")) {
            String pathPart = url.substring("jdbc:sqlite:".length()).trim();
            int questionIndex = pathPart.indexOf('?');
            if (questionIndex >= 0) {
                pathPart = pathPart.substring(0, questionIndex);
            }
            if (!":memory:".equals(pathPart) && !pathPart.isEmpty()) {
                try {
                    Path path = Path.of(pathPart);
                    if (!Files.exists(path)) {
                        throw new AplicationException("El archivo de base de datos no existe: " + pathPart);
                    }
                } catch (AplicationException ae) {
                    throw ae;
                } catch (Exception e) {
                    throw new AplicationException("Ruta de base de datos no válida: " + pathPart, e);
                }
            }
        }
        try {
            Connection con = DriverManager.getConnection(url);
            try (Statement stmt = con.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            } catch (SQLException e) {
                try { con.close(); } catch (SQLException ignored) {}
                throw e;
            }
            return con;
        } catch (SQLException e) {
            throw new AplicationException("Error al abrir la conexión con " + url, e);
        }
    }
}

