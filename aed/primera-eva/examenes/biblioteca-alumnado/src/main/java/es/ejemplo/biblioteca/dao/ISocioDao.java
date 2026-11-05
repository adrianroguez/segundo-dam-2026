package es.ejemplo.biblioteca.dao;

import java.util.*;

import es.ejemplo.biblioteca.modelo.*;

/**
 * Interfaz para las operaciones de acceso a datos de socios.
 */
public interface ISocioDao {

    /**
     * Obtiene la lista de todos los socios registrados.
     *
     * @return Lista de socios.
     */
    List<Socio> listar();

    /**
     * Busca un socio por su identificador unico.
     *
     * @param id Identificador del socio.
     * @return Optional con el socio encontrado o vacio si no existe.
     */
    Optional<Socio> buscarPorId(Integer id);

    /**
     * Inserta un nuevo socio en el sistema y asigna su identificador generado.
     *
     * @param entidad Socio a registrar.
     * @return Identificador asignado al nuevo socio.
     */
    int insertar(Socio entidad);

    /**
     * Actualiza la informacion de un socio en la base de datos.
     *
     * @param entidad Socio con los datos actualizados.
     * @return Numero de filas afectadas por la modificacion.
     */
    int actualizar(Socio entidad);

    /**
     * Elimina un socio del sistema a partir de su identificador.
     *
     * @param id Identificador del socio a eliminar.
     * @return Numero de filas eliminadas.
     */
    int eliminar(Integer id);

    /**
     * Busca un socio a partir de su direccion de correo electronico.
     *
     * @param email Correo electronico del socio.
     * @return Optional con el socio encontrado o vacio si no coincide ninguno.
     */
    Optional<Socio> buscarPorEmail(String email);

    /**
     * Lista los socios cuya fecha de alta sea posterior o igual a la fecha indicada.
     *
     * @param fecha Fecha limite inferior en formato texto.
     * @return Lista de socios dados de alta a partir de dicha fecha.
     */
    List<Socio> listarAltasDesde(String fecha);
}
