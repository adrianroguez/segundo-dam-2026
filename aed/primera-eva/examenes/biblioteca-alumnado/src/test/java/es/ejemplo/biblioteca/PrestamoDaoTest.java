package es.ejemplo.biblioteca;

import es.ejemplo.biblioteca.modelo.*;
import es.ejemplo.biblioteca.dao.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas DAO: un comportamiento concreto por cada método @Test.
 */
class PrestamoDaoTest extends BaseDaoTest {
    /**
     * Lista vacía sin préstamos.
     */
    @Test
    void buscarTodosDevuelveListaVaciaTest() {
        assertTrue(prestamoDao.listar().isEmpty());
    }

    /**
     * Inserción de préstamo genera ID.
     */
    @Test
    void insertarRetornaIdTest() {
        assertTrue(insertarPrestamoDePrueba() > 0);
    }

    /**
     * Recupera préstamo existente.
     */
    @Test
    void buscarPorIdExistenteTest() {
        int id = insertarPrestamoDePrueba();
        assertEquals(id, prestamoDao.buscarPorId(id).orElseThrow().getId());
    }

    /**
     * Préstamo inexistente devuelve vacío.
     */
    @Test
    void buscarPorIdAusenteTest() {
        assertTrue(prestamoDao.buscarPorId(9999).isEmpty());
    }

    /**
     * ID nulo devuelve vacío.
     */
    @Test
    void buscarPorIdNuloTest() {
        assertTrue(prestamoDao.buscarPorId(null).isEmpty());
    }

    /**
     * Lista dos préstamos.
     */
    @Test
    void buscarTodosDevuelveDosTest() {
        insertarPrestamoDePrueba();
        insertarPrestamoDePrueba();
        assertEquals(2, prestamoDao.listar().size());
    }

    /**
     * Agrupación cuenta un préstamo.
     */
    @Test
    void contarPrestamosPorSocioCoincideTest() {
        insertarPrestamoDePrueba();
        assertEquals(1L, prestamoDao.contarPrestamosPorSocio().get(0).getTotalPrestamos());
    }

    /**
     * Agrupación incluye socio sin préstamo con cero.
     */
    @Test
    void contarPrestamosPorSocioSinPrestamosTest() {
        insertarSocioDePrueba();
        assertEquals(0L, prestamoDao.contarPrestamosPorSocio().get(0).getTotalPrestamos());
    }

    /**
     * Agrupación de varios socios ordena por total descendente.
     */
    @Test
    void contarPrestamosPorSocioVariosTest() {
        int l = insertarLibroDePrueba();
        int a = insertarSocioDePrueba();
        int b = socioDao.insertar(new Socio(null, "Bea", "bea@ejemplo.es", "2026-01-01"));
        prestamoDao.insertar(new Prestamo(null, l, a, "2026-02-01", null));
        prestamoDao.insertar(new Prestamo(null, l, a, "2026-02-02", null));
        prestamoDao.insertar(new Prestamo(null, l, b, "2026-02-03", null));
        assertEquals(List.of(2L, 1L), prestamoDao.contarPrestamosPorSocio().stream().map(PrestamosPorSocio::getTotalPrestamos).toList());
    }

    /**
     * Subconsulta devuelve préstamos de autor coincidente.
     */
    @Test
    void listarPrestamosPorAutorCoincideTest() {
        insertarPrestamoDePrueba();
        assertEquals(1, prestamoDao.listarPrestamosPorAutor("Cervantes").size());
    }

    /**
     * Subconsulta devuelve vacío sin coincidencias.
     */
    @Test
    void listarPrestamosPorAutorSinCoincidenciaTest() {
        insertarPrestamoDePrueba();
        assertTrue(prestamoDao.listarPrestamosPorAutor("Desconocido").isEmpty());
    }

    /**
     * Actualiza fecha de devolución.
     */
    @Test
    void actualizarFechaDevolucionTest() {
        int id = insertarPrestamoDePrueba();
        Prestamo p = prestamoDao.buscarPorId(id).orElseThrow();
        p.setFechaDevolucion("2026-02-08");
        assertEquals(1, prestamoDao.actualizar(p));
        assertEquals("2026-02-08", prestamoDao.buscarPorId(id).orElseThrow().getFechaDevolucion());
    }

    /**
     * Actualizar préstamo desconocido afecta cero filas.
     */
    @Test
    void actualizarAusenteTest() {
        assertEquals(0, prestamoDao.actualizar(new Prestamo(9999, 1, 1, "2026-01-01", null)));
    }

    /**
     * Elimina préstamo existente.
     */
    @Test
    void eliminarExistenteTest() {
        assertEquals(1, prestamoDao.eliminar(insertarPrestamoDePrueba()));
    }

    /**
     * Clave foránea a socio inexistente se rechaza.
     */
    @Test
    void socioInexistenteRechazadoTest() {
        int l = insertarLibroDePrueba();
        assertThrows(RuntimeException.class, () -> prestamoDao.insertar(new Prestamo(null, l, 9999, "2026-02-01", null)));
    }

    /**
     * Interfaz expone la agrupación.
     */
    @Test
    void interfazDAOPermiteConsultasTest() {
        IPrestamoDao dao = prestamoDao;
        insertarSocioDePrueba();
        assertEquals(1, dao.contarPrestamosPorSocio().size());
    }
}
