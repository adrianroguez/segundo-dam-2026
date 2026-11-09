package es.ejemplo.biblioteca.dao;

import es.ejemplo.biblioteca.modelo.Autor;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import es.ejemplo.biblioteca.orm.entity.AutorEntity;
import es.ejemplo.biblioteca.orm.mapper.AutorMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Objeto de acceso a datos para la entidad de autores de obras en la biblioteca.
 * Proporciona metodos de persistencia, actualizacion y consultas agrupadas sobre autores.
 */
public class AutorDao implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(AutorDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Inicializa una nueva instancia de AutorDao creando su propia factoría ORM.
     *
     * @param url direccion de conexion JDBC hacia la base de datos SQLite
     */
    public AutorDao(String url) {
        this.managerFactory = OrmFactory.crear(url);
        this.ownFactory = true;
    }

    /**
     * Inicializa una nueva instancia de AutorDao reutilizando una factoría existente.
     *
     * @param factory factoria de EntityManager configurada para el ciclo de vida
     */
    public AutorDao(EntityManagerFactory factory) {
        this.managerFactory = factory;
        this.ownFactory = false;
    }

    private void anotarError(String operacion, Exception err) {
        LOG.log(Level.WARNING, operacion + " no se pudo completar: " + err.getMessage());
    }

    private void revertirTransaccion(EntityTransaction tx) {
        if (tx.isActive()) {
            tx.rollback();
        }
    }

    private <T> List<T> ejecutarConsulta(String jpql, Class<T> clase) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).getResultList();
        } catch (Exception ex) {
            anotarError("ejecutarConsulta", ex);
            return List.of();
        }
    }

    private boolean validar(Autor dato) {
        return dato != null && dato.getNombre() != null && !dato.getNombre().isBlank();
    }

    /**
     * Obtiene la coleccion de todos los autores registrados en la base de datos.
     *
     * @return lista completa de autores ordenados alfabeticamente o vacia ante fallos
     */
    public List<Autor> listar() {
        return ejecutarConsulta("SELECT a FROM AutorEntity a ORDER BY a.nombre ASC", AutorEntity.class)
                .stream().map(AutorMapper::toDomain).toList();
    }

    /**
     * Busca un autor concreto por su identificador primario dentro del catalogo.
     *
     * @param id clave numerica que identifica univocamente al autor consultado
     * @return Optional con el autor localizado o vacio si no existe o se produce error
     */
    public Optional<Autor> buscarPorId(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        try (EntityManager em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(AutorEntity.class, id)).map(AutorMapper::toDomain);
        } catch (Exception ex) {
            anotarError("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Da de alta un nuevo autor en la base de datos garantizando atomicidad.
     *
     * @param dato modelo que contiene los datos validos del autor que se va a insertar
     * @return identificador generado positivo si tuvo exito o 0 si ocurrio un error
     */
    public int insertar(Autor dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            AutorEntity entidad = AutorMapper.toEntity(dato);
            entidad.setIdAutor(null);
            em.persist(entidad);
            em.flush();
            tx.commit();
            return entidad.getIdAutor();
        } catch (Exception ex) {
            revertirTransaccion(tx);
            anotarError("insertar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Modifica los datos descriptivos de un autor registrado previamente en el sistema.
     *
     * @param dato modelo del autor con valores actualizados e identificador existente
     * @return 1 si el autor fue modificado satisfactoriamente o 0 en caso de fallo
     */
    public int actualizar(Autor dato) {
        if (!validar(dato) || dato.getIdAutor() == null) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            AutorEntity entidad = em.find(AutorEntity.class, dato.getIdAutor());
            if (entidad == null) {
                revertirTransaccion(tx);
                return 0;
            }
            entidad.setNombre(dato.getNombre());
            entidad.setNacionalidad(dato.getNacionalidad());
            entidad.setAnioNacimiento(dato.getAnioNacimiento());
            tx.commit();
            return 1;
        } catch (Exception ex) {
            revertirTransaccion(tx);
            anotarError("actualizar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    private boolean tieneLibros(EntityManager em, Integer id) {
        return em.createQuery("SELECT COUNT(l) FROM LibroEntity l WHERE l.idAutor = :id", Long.class)
                .setParameter("id", id).getSingleResult() > 0;
    }

    /**
     * Elimina el registro de un autor verificando que no posea libros asociados.
     *
     * @param id identificador numerico del autor que se pretende borrar del catalogo
     * @return 1 si la operacion fue exitosa o 0 si no existe o tiene libros vinculados
     */
    public int eliminar(Integer id) {
        if (id == null) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            AutorEntity entidad = em.find(AutorEntity.class, id);
            if (entidad == null || tieneLibros(em, id)) {
                revertirTransaccion(tx);
                return 0;
            }
            em.remove(entidad);
            tx.commit();
            return 1;
        } catch (Exception ex) {
            revertirTransaccion(tx);
            anotarError("eliminar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Recupera todos los autores pertenecientes a una nacionalidad especifica.
     *
     * @param nacionalidad nombre del pais o nacionalidad por el cual filtrar
     * @return lista de autores coincidentes o lista vacia ante incidencias o ausencia
     */
    public List<Autor> buscarPorNacionalidad(String nacionalidad) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(
                            "SELECT a FROM AutorEntity a WHERE a.nacionalidad = :nac ORDER BY a.nombre ASC",
                            AutorEntity.class).setParameter("nac", nacionalidad).getResultList()
                    .stream().map(AutorMapper::toDomain).toList();
        } catch (Exception ex) {
            anotarError("buscarPorNacionalidad", ex);
            return List.of();
        }
    }

    /**
     * Cuenta la cantidad total de libros registrados que pertenecen a un autor dado.
     *
     * @param idAutor identificador del autor para el cual se computa el total de libros
     * @return numero de libros asociados en el catalogo o 0 si ocurre un problema
     */
    public long contarLibrosPorAutor(int idAutor) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery("SELECT COUNT(l) FROM LibroEntity l WHERE l.idAutor = :id", Long.class)
                    .setParameter("id", idAutor).getSingleResult();
        } catch (Exception ex) {
            anotarError("contarLibrosPorAutor", ex);
            return 0L;
        }
    }

    /**
     * Obtiene el listado de autores y el recuento de sus obras, incluyendo autores sin libros.
     *
     * @return lista de arreglos con id del autor, nombre y cantidad de libros publicados
     */
    public List<Object[]> autoresConRecuento() {
        return ejecutarConsulta("SELECT a.idAutor, a.nombre, COUNT(l.idLibro) FROM AutorEntity a "
                + "LEFT JOIN LibroEntity l ON a.idAutor = l.idAutor "
                + "GROUP BY a.idAutor, a.nombre ORDER BY a.nombre ASC", Object[].class);
    }

    /**
     * Selecciona autores cuyas obras han sido prestadas a tres o mas socios diferentes.
     *
     * @return lista de autores que cumplen el umbral de tres o mas lectores distintos
     */
    public List<Autor> autoresLeidosPorTresSocios() {
        return ejecutarConsulta("SELECT a FROM AutorEntity a JOIN LibroEntity l ON a.idAutor = l.idAutor "
                + "JOIN PrestamoEntity p ON l.idLibro = p.idLibro "
                + "GROUP BY a.idAutor, a.nombre, a.nacionalidad, a.anioNacimiento "
                + "HAVING COUNT(DISTINCT p.idSocio) >= 3 ORDER BY a.nombre ASC", AutorEntity.class)
                .stream().map(AutorMapper::toDomain).toList();
    }

    /**
     * Cierra la factoria de gestores de persistencia asociada si fue creada por el DAO.
     */
    public void close() {
        if (ownFactory) {
            managerFactory.close();
        }
    }
}
