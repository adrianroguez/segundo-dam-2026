package es.ejemplo.biblioteca.dao;

import java.util.*;

import es.ejemplo.biblioteca.modelo.*;

/**
 * Interfaz para las operaciones de acceso a datos de prestamos.
 */
public interface IPrestamoDao {

    /**
     * Obtiene la lista de todos los prestamos registrados.
     *
     * @return Lista de prestamos.
     */
    List<Prestamo> listar();

    /**
     * Busca un prestamo segun su identificador unico.
     *
     * @param id Identificador del prestamo.
     * @return Optional con el prestamo encontrado o vacio si no existe.
     */
    Optional<Prestamo> buscarPorId(Integer id);

    /**
     * Registra un nuevo prestamo y asigna el identificador generado.
     *
     * @param entidad Prestamo a registrar.
     * @return Identificador del prestamo insertado.
     */
    int insertar(Prestamo entidad);

    /**
     * Actualiza los datos de un prestamo existente.
     *
     * @param entidad Prestamo con la informacion actualizada.
     * @return Numero de filas afectadas por la actualizacion.
     */
    int actualizar(Prestamo entidad);

    /**
     * Elimina un prestamo a partir de su identificador.
     *
     * @param id Identificador del prestamo a eliminar.
     * @return Numero de filas eliminadas.
     */
    int eliminar(Integer id);

    /**
     * Cuenta la cantidad total de prestamos realizados por cada socio,
     * incluyendo aquellos socios sin prestamos y ordenando de mayor a menor.
     *
     * @return Lista con el recuento de prestamos agrupados por socio.
     */
    List<PrestamosPorSocio> contarPrestamosPorSocio();

    /**
     * Obtiene los prestamos asociados a libros escritos por el autor indicado.
     *
     * @param autor Nombre del autor del libro.
     * @return Lista de prestamos correspondientes al autor especificado.
     */
    List<Prestamo> listarPrestamosPorAutor(String autor);
}
