package es.ejemplo.biblioteca;

import es.ejemplo.biblioteca.modelo.*;
import es.ejemplo.biblioteca.dao.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas DAO: un comportamiento concreto por cada método @Test.
 */
class LibroDaoTest extends BaseDaoTest {
    /**
     * Lista vacía si no existen libros.
     */
    @Test
    void buscarTodosDevuelveListaVaciaTest() {
        assertTrue(libroDao.listar().isEmpty());
    }

    /**
     * Insertar genera un ID positivo.
     */
    @Test
    void insertarDevuelveIdPositivoTest() {
        assertTrue(insertarLibroDePrueba() > 0);
    }

    /**
     * Dos inserciones producen identificadores distintos.
     */
    @Test
    void insertarDosLibrosGeneraIdsDistintosTest() {
        int a = insertarLibroDePrueba();
        int b = insertarLibroDePrueba();
        assertNotEquals(a, b);
    }

    /**
     * Busca un libro existente.
     */
    @Test
    void buscarPorIdDevuelveLibroExistenteTest() {
        int id = insertarLibroDePrueba();
        assertEquals("El Quijote", libroDao.buscarPorId(id).orElseThrow().getTitulo());
    }

    /**
     * Un identificador inexistente no devuelve libro.
     */
    @Test
    void buscarPorIdInexistenteDevuelveVacioTest() {
        assertTrue(libroDao.buscarPorId(9999).isEmpty());
    }

    /**
     * Un identificador nulo devuelve vacío.
     */
    @Test
    void buscarPorIdNuloDevuelveVacioTest() {
        assertTrue(libroDao.buscarPorId(null).isEmpty());
    }

    /**
     * Lista ordenada por identificador ascendente.
     */
    @Test
    void buscarTodosDevuelveLibrosOrdenadosTest() {
        int a = insertarLibroDePrueba();
        int b = insertarLibroDePrueba();
        assertEquals(List.of(a, b), libroDao.listar().stream().map(Libro::getId).toList());
    }

    /**
     * Actualiza el título del libro.
     */
    @Test
    void actualizarModificaTituloTest() {
        int id = insertarLibroDePrueba();
        libroDao.actualizar(new Libro(id, "Nuevo", "Cervantes", 1605));
        assertEquals("Nuevo", libroDao.buscarPorId(id).orElseThrow().getTitulo());
    }

    /**
     * Actualiza el autor del libro.
     */
    @Test
    void actualizarModificaAutorTest() {
        int id = insertarLibroDePrueba();
        libroDao.actualizar(new Libro(id, "El Quijote", "Otro", 1605));
        assertEquals("Otro", libroDao.buscarPorId(id).orElseThrow().getAutor());
    }

    /**
     * Actualizar un ID inexistente afecta a cero filas.
     */
    @Test
    void actualizarLibroInexistenteDevuelveCeroTest() {
        assertEquals(0, libroDao.actualizar(new Libro(9999, "X", "Y", 2000)));
    }

    /**
     * Actualizar sin ID se considera inválido.
     */
    @Test
    void actualizarSinIdLanzaExcepcionTest() {
        assertThrows(IllegalArgumentException.class, () -> libroDao.actualizar(new Libro(null, "X", "Y", 2000)));
    }

    /**
     * Eliminar un libro existente afecta a una fila.
     */
    @Test
    void eliminarLibroExistenteDevuelveUnoTest() {
        assertEquals(1, libroDao.eliminar(insertarLibroDePrueba()));
    }

    /**
     * Borrar por segunda vez afecta a cero filas.
     */
    @Test
    void eliminarLibroRepetidoDevuelveCeroTest() {
        int id = insertarLibroDePrueba();
        libroDao.eliminar(id);
        assertEquals(0, libroDao.eliminar(id));
    }

    /**
     * Eliminar con identificador nulo se rechaza.
     */
    @Test
    void eliminarIdNuloLanzaExcepcionTest() {
        assertThrows(IllegalArgumentException.class, () -> libroDao.eliminar(null));
    }

    /**
     * La base rechaza título obligatorio nulo.
     */
    @Test
    void insertarTituloNuloLanzaExcepcionTest() {
        assertThrows(RuntimeException.class, () -> libroDao.insertar(new Libro(null, null, "Autor", 2000)));
    }

    /**
     * Comprueba polimorfismo a través de la interfaz.
     */
    @Test
    void interfazLibroDaoPermiteInsertarTest() {
        ILibroDao dao = libroDao;
        assertTrue(dao.insertar(new Libro(null, "Título", "Autor", 2000)) > 0);
    }
}
