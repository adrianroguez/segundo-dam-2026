package es.ejemplo.biblioteca.dao;

import es.ejemplo.biblioteca.modelo.Reserva;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import es.ejemplo.biblioteca.orm.entity.ReservaEntity;
import es.ejemplo.biblioteca.orm.mapper.ReservaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Objeto de acceso a datos para gestionar solicitudes de reserva de libros.
 * Permite consultar reservas pendientes, su estado de atencion y su prioridad.
 */
public class ReservaDao implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(ReservaDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Inicializa una nueva instancia de ReservaDao creando su factoria ORM.
     *
     * @param url direccion de conexion JDBC hacia la base de datos SQLite
     */
    public ReservaDao(String url) {
        this.managerFactory = OrmFactory.crear(url);
        this.ownFactory = true;
    }

    /**
     * Inicializa una nueva instancia de ReservaDao reutilizando una factoria dada.
     *
     * @param factory factoria de EntityManager configurada en el contenedor
     */
    public ReservaDao(EntityManagerFactory factory) {
        this.managerFactory = factory;
        this.ownFactory = false;
    }

    private void volcarIncidencia(String contexto, Exception causa) {
        LOG.log(Level.WARNING, contexto + " no se pudo completar: " + causa.getMessage());
    }

    private void descartar(EntityTransaction t) {
        if (t.isActive()) {
            t.rollback();
        }
    }

    private <T> List<T> recuperarLista(String jpql, Class<T> clase) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).getResultList();
        } catch (Exception ex) {
            volcarIncidencia("recuperarLista", ex);
            return List.of();
        }
    }

    private <T> List<T> recuperarCon(String jpql, Class<T> clase, String clave, Object valor) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).setParameter(clave, valor).getResultList();
        } catch (Exception ex) {
            volcarIncidencia("recuperarCon", ex);
            return List.of();
        }
    }

    private boolean validar(Reserva dato) {
        try {
            return (dato.getIdSocio() != null) & (dato.getIdLibro() != null)
                    & (dato.getFechaReserva() != null)
                    & java.util.Set.of("PENDIENTE", "ATENDIDA", "CANCELADA").contains(dato.getEstado());
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Lista todas las reservas almacenadas en la base de datos por identificador.
     *
     * @return coleccion con todas las reservas persistidas o lista vacia ante fallo
     */
    public List<Reserva> listar() {
        return recuperarLista("SELECT r FROM ReservaEntity r ORDER BY r.idReserva ASC", ReservaEntity.class)
                .stream().map(ReservaMapper::toDomain).toList();
    }

    /**
     * Busca una reserva especifica a partir de su clave primaria identificadora.
     *
     * @param id identificador numerico correspondiente a la reserva consultada
     * @return reserva encontrada dentro de Optional o vacio si no existe en BD
     */
    public Optional<Reserva> buscarPorId(Integer id) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(ReservaEntity.class, id)).map(ReservaMapper::toDomain);
        } catch (Exception ex) {
            volcarIncidencia("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Registra una nueva reserva validando la existencia del socio y del libro.
     *
     * @param dato modelo que contiene los datos validos de la reserva a crear
     * @return identificador generado positivo en caso de exito o 0 si ocurre fallo
     */
    public int insertar(Reserva dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ReservaEntity entidad = ReservaMapper.toEntity(dato);
            entidad.setIdReserva(null);
            em.persist(entidad);
            em.flush();
            tx.commit();
            return entidad.getIdReserva();
        } catch (Exception ex) {
            descartar(tx);
            volcarIncidencia("insertar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza el estado o detalles de una reserva previamente registrada.
     *
     * @param dato modelo de reserva con modificaciones e identificador existente
     * @return 1 si la fila fue actualizada con exito o 0 si no existe o hay error
     */
    public int actualizar(Reserva dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ReservaEntity target = em.find(ReservaEntity.class, dato.getIdReserva());
            if (target == null) {
                descartar(tx);
                return 0;
            }
            target.setIdSocio(dato.getIdSocio());
            target.setIdLibro(dato.getIdLibro());
            target.setFechaReserva(dato.getFechaReserva());
            target.setEstado(dato.getEstado());
            tx.commit();
            return 1;
        } catch (Exception ex) {
            descartar(tx);
            volcarIncidencia("actualizar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Suprime de forma atómica una reserva a partir de su identificador primario.
     *
     * @param id identificador numerico unico de la reserva que se eliminara
     * @return 1 si se borro satisfactoriamente o 0 si no existe o ocurre incidencia
     */
    public int eliminar(Integer id) {
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ReservaEntity item = em.find(ReservaEntity.class, id);
            if (item == null) {
                descartar(tx);
                return 0;
            }
            em.remove(item);
            tx.commit();
            return 1;
        } catch (Exception ex) {
            descartar(tx);
            volcarIncidencia("eliminar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene aquellas reservas que se encuentran actualmente en estado pendiente.
     *
     * @return lista de reservas pendientes ordenadas por fecha de solicitud ascendente
     */
    public List<Reserva> listarPendientes() {
        return recuperarLista(
                "SELECT r FROM ReservaEntity r WHERE r.estado = 'PENDIENTE' ORDER BY r.fechaReserva ASC",
                ReservaEntity.class).stream().map(ReservaMapper::toDomain).toList();
    }

    /**
     * Recupera todas las reservas tramitadas historicamente por un socio concreto.
     *
     * @param idSocio identificador numerico unico del socio cuyas reservas se buscan
     * @return lista de reservas asociadas al socio ordenadas cronologicamente
     */
    public List<Reserva> buscarPorSocio(int idSocio) {
        return recuperarCon("SELECT r FROM ReservaEntity r WHERE r.idSocio = :id ORDER BY r.fechaReserva ASC",
                ReservaEntity.class, "id", idSocio).stream().map(ReservaMapper::toDomain).toList();
    }

    /**
     * Obtiene el listado de reservas asociadas a un libro determinado del catalogo.
     *
     * @param idLibro identificador numerico del libro para el cual se listan reservas
     * @return lista de reservas realizadas sobre el libro ordenadas por antiguedad
     */
    public List<Reserva> listarPorLibro(int idLibro) {
        return recuperarCon("SELECT r FROM ReservaEntity r WHERE r.idLibro = :id ORDER BY r.fechaReserva ASC",
                ReservaEntity.class, "id", idLibro).stream().map(ReservaMapper::toDomain).toList();
    }

    /**
     * Cuenta la cantidad total de reservas que estan en espera de ser atendidas.
     *
     * @return recuento numerico de reservas pendientes o 0 si no hay o surge error
     */
    public long contarReservasPendientes() {
        return recuperarLista("SELECT COUNT(r) FROM ReservaEntity r WHERE r.estado = 'PENDIENTE'", Long.class)
                .stream().findFirst().orElse(0L);
    }

    /**
     * Consulta reservas pendientes con los nombres completos del socio, libro y autor.
     *
     * @return lista de tuplas con id de reserva, socio, titulo de obra, autor y fecha
     */
    public List<Object[]> reservasConDetalles() {
        return recuperarLista("SELECT r.idReserva, s.nombre, l.titulo, a.nombre, r.fechaReserva "
                + "FROM ReservaEntity r JOIN r.socio s JOIN r.libro l JOIN l.autor a "
                + "WHERE r.estado = 'PENDIENTE' ORDER BY r.fechaReserva ASC", Object[].class);
    }

    /**
     * Determina la reserva pendiente mas antigua que corresponde a cada obra solicitada.
     *
     * @return lista de arreglos conteniendo id de libro, titulo, reserva, socio y fecha
     */
    public List<Object[]> prioridadReservas() {
        return recuperarLista("SELECT r.idLibro, l.titulo, r.idReserva, s.nombre, r.fechaReserva "
                + "FROM ReservaEntity r JOIN r.libro l JOIN r.socio s WHERE r.estado = 'PENDIENTE' "
                + "AND r.fechaReserva = (SELECT MIN(r2.fechaReserva) FROM ReservaEntity r2 "
                + "WHERE r2.idLibro = r.idLibro AND r2.estado = 'PENDIENTE') "
                + "ORDER BY r.fechaReserva ASC", Object[].class);
    }

    /**
     * Cierra la factoria de gestores de persistencia asociada si es propia del DAO.
     */
    public void close() {
        if (ownFactory) {
            managerFactory.close();
        }
    }
}
