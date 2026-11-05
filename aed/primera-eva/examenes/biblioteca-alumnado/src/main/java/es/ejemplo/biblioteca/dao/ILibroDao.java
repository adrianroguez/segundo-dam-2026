package es.ejemplo.biblioteca.dao;

import java.util.*;

import es.ejemplo.biblioteca.modelo.*;

/**
 * Interfaz para las operaciones de acceso a datos de libros.
 */
public interface ILibroDao {

    /**
     * Obtiene la lista completa de libros almacenados en la base de datos.
     *
     * @return Lista de libros disponibles.
     */
    List<Libro> listar();

    /**
     * Busca un libro a partir de su identificador unico.
     *
     * @param id Identificador del libro.
     * @return Optional con el libro encontrado o vacio si no existe.
     */
    Optional<Libro> buscarPorId(Integer id);

    /**
     * Inserta un nuevo libro en la base de datos y asigna su identificador generado.
     *
     * @param entidad Libro que se desea registrar.
     * @return Identificador generado para el nuevo libro.
     */
    int insertar(Libro entidad);

    /**
     * Actualiza los datos de un libro existente en la base de datos.
     *
     * @param entidad Libro con los datos modificados.
     * @return Numero de filas afectadas por la operacion.
     */
    int actualizar(Libro entidad);

    /**
     * Elimina un libro de la base de datos a partir de su identificador.
     *
     * @param id Identificador del libro que se desea eliminar.
     * @return Numero de filas afectadas por la operacion.
     */
    int eliminar(Integer id);
}
