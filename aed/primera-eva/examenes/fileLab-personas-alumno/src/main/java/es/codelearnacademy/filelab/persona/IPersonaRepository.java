package es.codelearnacademy.filelab.persona;

import es.codelearnacademy.filelab.model.Persona;
import es.codelearnacademy.filelab.repository.IRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de personas que permite consultas especificas.
 */
public interface IPersonaRepository extends IRepository<Persona, String> {
    /**
     * Busca personas con edad mayor o igual a la edad minima indicada.
     *
     * @param edad edad minima inclusiva
     * @return lista de personas coincidentes; lista vacia si no hay resultados
     */
    default List<Persona> findByEdadMinima(int edad) {
        return findAll().stream()
                .filter(p -> p.edad() >= edad)
                .toList();
    }

    /**
     * Consulta personas segun su estado activo o inactivo.
     *
     * @param activo true para activas y false para inactivas
     * @return lista de personas coincidentes; vacia si no hay coincidencias
     */
    default List<Persona> findByActivo(boolean activo) {
        return findAll().stream()
                .filter(p -> p.activo() == activo)
                .toList();
    }
}