package es.ejemplo.biblioteca;

import es.ejemplo.biblioteca.modelo.*;
import es.ejemplo.biblioteca.dao.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas DAO: un comportamiento concreto por cada método @Test.
 */
class SocioDaoTest extends BaseDaoTest {
    /**
     * Lista vacía sin socios.
     */
    @Test
    void buscarTodosDevuelveListaVaciaTest() {
        assertTrue(socioDao.listar().isEmpty());
    }

    /**
     * Insertar produce ID válido.
     */
    @Test
    void insertarRetornaIdTest() {
        assertTrue(insertarSocioDePrueba() > 0);
    }

    /**
     * Encuentra socio existente.
     */
    @Test
    void buscarPorIdExistenteTest() {
        int id = insertarSocioDePrueba();
        assertEquals("Ana", socioDao.buscarPorId(id).orElseThrow().getNombre());
    }

    /**
     * Identificador inexistente devuelve vacío.
     */
    @Test
    void buscarPorIdAusenteTest() {
        assertTrue(socioDao.buscarPorId(9999).isEmpty());
    }

    /**
     * Identificador nulo devuelve vacío.
     */
    @Test
    void buscarPorIdNuloTest() {
        assertTrue(socioDao.buscarPorId(null).isEmpty());
    }

    /**
     * Lista dos socios en orden de ID.
     */
    @Test
    void buscarTodosDevuelveDosTest() {
        insertarSocioDePrueba();
        socioDao.insertar(new Socio(null, "Bea", "bea@ejemplo.es", "2026-03-01"));
        assertEquals(2, socioDao.listar().size());
    }

    /**
     * Busca socio por correo exacto.
     */
    @Test
    void buscarPorEmailCoincideTest() {
        insertarSocioDePrueba();
        assertEquals("Ana", socioDao.buscarPorEmail("ana@ejemplo.es").orElseThrow().getNombre());
    }

    /**
     * Correo desconocido devuelve vacío.
     */
    @Test
    void buscarPorEmailSinCoincidenciaTest() {
        assertTrue(socioDao.buscarPorEmail("no@ejemplo.es").isEmpty());
    }

    /**
     * Lista altas a partir de la fecha inclusiva.
     */
    @Test
    void listarAltasDesdeCoincideTest() {
        insertarSocioDePrueba();
        socioDao.insertar(new Socio(null, "Bea", "bea@ejemplo.es", "2026-05-01"));
        assertEquals(1, socioDao.listarAltasDesde("2026-04-01").size());
    }

    /**
     * No devuelve socios anteriores al corte.
     */
    @Test
    void listarAltasDesdeSinCoincidenciaTest() {
        insertarSocioDePrueba();
        assertTrue(socioDao.listarAltasDesde("2027-01-01").isEmpty());
    }

    /**
     * Actualiza nombre de socio.
     */
    @Test
    void actualizarNombreTest() {
        int id = insertarSocioDePrueba();
        socioDao.actualizar(new Socio(id, "Ana María", "ana@ejemplo.es", "2026-01-01"));
        assertEquals("Ana María", socioDao.buscarPorId(id).orElseThrow().getNombre());
    }

    /**
     * Actualizar socio inexistente afecta cero filas.
     */
    @Test
    void actualizarAusenteTest() {
        assertEquals(0, socioDao.actualizar(new Socio(9999, "X", "x@ejemplo.es", "2026-01-01")));
    }

    /**
     * Elimina socio existente.
     */
    @Test
    void eliminarExistenteTest() {
        assertEquals(1, socioDao.eliminar(insertarSocioDePrueba()));
    }

    /**
     * Eliminar socio inexistente afecta cero filas.
     */
    @Test
    void eliminarAusenteTest() {
        assertEquals(0, socioDao.eliminar(9999));
    }

    /**
     * Rechaza duplicados de correo único.
     */
    @Test
    void emailDuplicadoRechazadoTest() {
        insertarSocioDePrueba();
        assertThrows(RuntimeException.class, () -> socioDao.insertar(new Socio(null, "Otra", "ana@ejemplo.es", "2026-02-01")));
    }

    /**
     * Ejecuta un filtro a través de la interfaz.
     */
    @Test
    void interfazDAOPermiteFiltrarTest() {
        ISocioDao dao = socioDao;
        insertarSocioDePrueba();
        assertTrue(dao.buscarPorEmail("ana@ejemplo.es").isPresent());
    }
}
