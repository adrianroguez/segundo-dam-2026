package es.ejemplo.biblioteca;

import es.ejemplo.biblioteca.dao.*;
import es.ejemplo.biblioteca.modelo.*;
import org.junit.jupiter.api.*;

import java.nio.file.*;
import java.sql.*;
import java.util.*;

/**
 * Preparación común: base SQLite temporal aislada por prueba; nunca toca la base de ejemplo.
 */
abstract class BaseDaoTest {
    protected Path archivo;
    protected String url;
    private int contadorSociosDePrueba;
    protected ILibroDao libroDao;
    protected ISocioDao socioDao;
    protected IPrestamoDao prestamoDao;

    @BeforeEach
    void prepararBaseDatosDePrueba() throws Exception {
        contadorSociosDePrueba = 0;
        archivo = Files.createTempFile("biblioteca-junit-", ".sqlite3");
        url = "jdbc:sqlite:" + archivo.toAbsolutePath();
        try (Connection cn = abrir(url); Statement st = cn.createStatement()) {
            String sql = new String(Objects.requireNonNull(getClass().getResourceAsStream("/schema.sql")).readAllBytes());
            for (String s : sql.split(";")) if (!s.isBlank()) st.execute(s);
        }
        libroDao = new LibroDao(url);
        socioDao = new SocioDao(url);
        prestamoDao = new PrestamoDao(url);
    }

    @AfterEach
    void eliminarBaseDatosTemporal() throws Exception {
        Files.deleteIfExists(archivo);
    }

    protected int insertarLibroDePrueba() {
        return libroDao.insertar(new Libro(null, "El Quijote", "Cervantes", 1605));
    }

    protected int insertarSocioDePrueba() {
        String email = contadorSociosDePrueba++ == 0 ? "ana@ejemplo.es" : "ana" + contadorSociosDePrueba + "@ejemplo.es";
        return socioDao.insertar(new Socio(null, "Ana", email, "2026-01-01"));
    }

    protected int insertarPrestamoDePrueba() {
        return prestamoDao.insertar(new Prestamo(null, insertarLibroDePrueba(), insertarSocioDePrueba(), "2026-02-01", null));
    }

    protected Connection abrir(String url) throws SQLException {
        Connection cn = DriverManager.getConnection(url);
        try (Statement st = cn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return cn;
    }
}
