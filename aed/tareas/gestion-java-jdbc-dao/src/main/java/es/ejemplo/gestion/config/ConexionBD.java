package es.ejemplo.gestion.config;

import es.ejemplo.gestion.exception.AplicationException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Abre una conexion en la base SQLite indicada por el constructor.
 */
public class ConexionBD {
    private final String url;

    public ConexionBD(String url) {
        String nuevaURL = url.substring("jdbc:sqlite:".length());
        this.url = url;
        Path path = Path.of(nuevaURL);

        if (!Files.exists(path)) {
            throw new AplicationException("No existe la base de datos");
        }
    }

    public Connection obtenerConexion() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new AplicationException("Error conectando con SQLite", e);
        }
    }
}