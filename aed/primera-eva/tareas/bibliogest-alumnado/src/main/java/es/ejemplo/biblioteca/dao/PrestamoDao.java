package es.ejemplo.biblioteca.dao;

import es.ejemplo.biblioteca.modelo.Prestamo;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import es.ejemplo.biblioteca.orm.entity.LibroEntity;
import es.ejemplo.biblioteca.orm.entity.PrestamoEntity;
import es.ejemplo.biblioteca.orm.entity.SocioEntity;
import es.ejemplo.biblioteca.orm.mapper.PrestamoMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Objeto de acceso a datos para la gestion de prestamos de libros a socios.
 * Controla altas atomicas, devoluciones, comprobacion de plazos y concurrencia.
 */
public class PrestamoDao implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(PrestamoDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Inicializa una nueva instancia de PrestamoDao creando su factoria ORM.
     *
     * @param url direccion de conexion JDBC hacia la base de datos SQLite
     */
    public PrestamoDao(String url) {
        this.managerFactory = OrmFactory.crear(url);
        this.ownFactory = true;
    }

    /**
     * Inicializa una nueva instancia de PrestamoDao reutilizando una factoria dada.
     *
     * @param factory factoria de EntityManager ya existente en el contexto
     */
    public PrestamoDao(EntityManagerFactory factory) {
        this.managerFactory = factory;
        this.ownFactory = false;
    }

    private void registrarLog(String op, Exception exc) {
        LOG.log(Level.WARNING, op + " no se pudo completar: " + exc.getMessage());
    }

    private void cancelar(EntityTransaction tx) {
        if (tx.isActive()) {
            tx.rollback();
        }
    }

    private <T> List<T> consultarRegistros(String jpql, Class<T> clase) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).getResultList();
        } catch (Exception ex) {
            registrarLog("consultarRegistros", ex);
            return List.of();
        }
    }

    private <T> List<T> consultarCon(String jpql, Class<T> clase, String par, Object valor) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).setParameter(par, valor).getResultList();
        } catch (Exception ex) {
            registrarLog("consultarCon", ex);
            return List.of();
        }
    }

    private boolean validar(Prestamo dato) {
        try {
            String p = dato.getFechaPrestamo();
            String l = dato.getFechaLimite();
            String d = dato.getFechaDevolucion();
            return (dato.getIdSocio() != null)
                    & (dato.getIdLibro() != null)
                    & (l.compareTo(p) >= 0)
                    & (d == null || d.compareTo(p) >= 0);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Lista todos los prestamos historicos y vigentes ordenados por identificador.
     *
     * @return coleccion con la totalidad de los prestamos o lista vacia ante fallo
     */
    public List<Prestamo> listar() {
        return consultarRegistros("SELECT p FROM PrestamoEntity p ORDER BY p.idPrestamo ASC", PrestamoEntity.class)
                .stream().map(PrestamoMapper::toDomain).toList();
    }

    /**
     * Busca un prestamo especifico dentro del sistema mediante su clave primaria.
     *
     * @param id identificador numerico correspondiente al prestamo consultado
     * @return prestamo localizado dentro de Optional o vacio si no se encuentra
     */
    public Optional<Prestamo> buscarPorId(Integer id) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(PrestamoEntity.class, id)).map(PrestamoMapper::toDomain);
        } catch (Exception ex) {
            registrarLog("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta directamente un registro de prestamo validando claves foraneas.
     *
     * @param dato modelo que contiene los datos del prestamo a persistir
     * @return clave autogenerada positiva en caso de exito o 0 si ocurre un error
     */
    public int insertar(Prestamo dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PrestamoEntity entidad = PrestamoMapper.toEntity(dato);
            entidad.setIdPrestamo(null);
            em.persist(entidad);
            em.flush();
            tx.commit();
            return entidad.getIdPrestamo();
        } catch (Exception ex) {
            cancelar(tx);
            registrarLog("insertar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza la informacion y fechas asociadas a un prestamo existente.
     *
     * @param dato modelo de prestamo modificado con identificador valido
     * @return 1 si el prestamo fue actualizado satisfactoriamente o 0 si no existe
     */
    public int actualizar(Prestamo dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PrestamoEntity entidad = em.find(PrestamoEntity.class, dato.getIdPrestamo());
            if (entidad == null) {
                cancelar(tx);
                return 0;
            }
            entidad.setIdSocio(dato.getIdSocio());
            entidad.setIdLibro(dato.getIdLibro());
            entidad.setFechaPrestamo(dato.getFechaPrestamo());
            entidad.setFechaLimite(dato.getFechaLimite());
            entidad.setFechaDevolucion(dato.getFechaDevolucion());
            tx.commit();
            return 1;
        } catch (Exception ex) {
            cancelar(tx);
            registrarLog("actualizar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Borra un prestamo registrado de la base de datos de manera atómica.
     *
     * @param id identificador numerico unico del prestamo que se eliminara
     * @return 1 si la operacion concluyo con exito o 0 si el prestamo no existe
     */
    public int eliminar(Integer id) {
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PrestamoEntity entidad = em.find(PrestamoEntity.class, id);
            if (entidad == null) {
                cancelar(tx);
                return 0;
            }
            em.remove(entidad);
            tx.commit();
            return 1;
        } catch (Exception ex) {
            cancelar(tx);
            registrarLog("eliminar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene el listado de prestamos asociados historicamente a un socio.
     *
     * @param idSocio identificador primario del socio cuyos prestamos se listan
     * @return lista de prestamos ordenados de mas recientes a mas antiguos
     */
    public List<Prestamo> listarPorSocio(int idSocio) {
        return consultarCon("SELECT p FROM PrestamoEntity p WHERE p.idSocio = :id ORDER BY p.fechaPrestamo DESC",
                PrestamoEntity.class, "id", idSocio).stream().map(PrestamoMapper::toDomain).toList();
    }

    /**
     * Recupera aquellos prestamos que permanecen activos y pendientes de devolucion.
     *
     * @return lista de prestamos activos cuya fecha de devolucion es nula
     */
    public List<Prestamo> listarActivos() {
        return consultarRegistros(
                "SELECT p FROM PrestamoEntity p WHERE p.fechaDevolucion IS NULL ORDER BY p.fechaPrestamo DESC",
                PrestamoEntity.class).stream().map(PrestamoMapper::toDomain).toList();
    }

    /**
     * Obtiene prestamos activos cuyo plazo ha caducado respecto a una fecha limite.
     *
     * @param fecha fecha de referencia en formato YYYY-MM-DD para evaluar retrasos
     * @return lista de prestamos activos cuya fecha limite es previa a la dada
     */
    public List<Prestamo> listarVencidos(String fecha) {
        return consultarCon("SELECT p FROM PrestamoEntity p WHERE p.fechaDevolucion IS NULL AND p.fechaLimite < :f "
                + "ORDER BY p.fechaLimite ASC", PrestamoEntity.class, "f", fecha)
                .stream().map(PrestamoMapper::toDomain).toList();
    }

    /**
     * Computa el recuento total de prestamos concedidos historicamente para un libro.
     *
     * @param idLibro identificador numerico unico del libro consultado
     * @return cantidad total de veces que se ha prestado el ejemplar o 0 ante fallo
     */
    public long contarPrestamosPorLibro(int idLibro) {
        return consultarCon("SELECT COUNT(p) FROM PrestamoEntity p WHERE p.idLibro = :idLibro", Long.class,
                "idLibro", idLibro).stream().findFirst().orElse(0L);
    }

    /**
     * Consulta el conjunto de prestamos detallando datos del socio, libro y fechas.
     *
     * @return lista de tuplas con id de prestamo, nombre de socio, libro y plazos
     */
    public List<Object[]> prestamosConSocioYLibro() {
        return consultarRegistros(
                "SELECT p.idPrestamo, s.nombre, l.titulo, p.fechaPrestamo, p.fechaLimite, p.fechaDevolucion "
                        + "FROM PrestamoEntity p JOIN p.socio s JOIN p.libro l ORDER BY p.fechaPrestamo DESC",
                Object[].class);
    }

    /**
     * Elabora el ranking mensual de libros mas solicitados en el periodo especificado.
     *
     * @param anioMes prefijo de fecha en formato YYYY-MM para acotar el periodo
     * @param limite cantidad maxima de resultados que compondran la clasificacion
     * @return lista de arreglos conteniendo titulo, autor y cantidad de prestamos
     */
    public List<Object[]> rankingMensual(String anioMes, int limite) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery("SELECT l.titulo, a.nombre, COUNT(p.idPrestamo) FROM PrestamoEntity p "
                            + "JOIN p.libro l JOIN l.autor a WHERE p.fechaPrestamo LIKE CONCAT(:am, '%') "
                            + "GROUP BY l.idLibro, l.titulo, a.nombre ORDER BY COUNT(p.idPrestamo) DESC",
                    Object[].class).setParameter("am", anioMes).setMaxResults(Math.max(limite, 1)).getResultList();
        } catch (Exception ex) {
            registrarLog("rankingMensual", ex);
            return List.of();
        }
    }

    private boolean stockDisponible(EntityManager em, int idSocio, int idLibro) {
        try {
            SocioEntity s = em.find(SocioEntity.class, idSocio);
            LibroEntity l = em.find(LibroEntity.class, idLibro);
            Long activos = em.createQuery(
                    "SELECT COUNT(p) FROM PrestamoEntity p WHERE p.idLibro = :id AND p.fechaDevolucion IS NULL",
                    Long.class).setParameter("id", idLibro).getSingleResult();
            return s.getActivo() & (activos < l.getEjemplares());
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Registra un prestamo de forma atomica comprobando socio activo y stock libre.
     *
     * @param idSocio identificador primario del socio solicitante
     * @param idLibro identificador primario del libro que se prestara
     * @param inicio fecha inicial de concesion del prestamo YYYY-MM-DD
     * @param limite fecha tope acordada para la devolucion YYYY-MM-DD
     * @return identificador generado positivo o 0 si no hay stock o socio inactivo
     */
    public synchronized int registrarPrestamo(int idSocio, int idLibro, String inicio, String limite) {
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            if (limite.compareTo(inicio) < 0) {
                return 0;
            }
            tx.begin();
            if (!stockDisponible(em, idSocio, idLibro)) {
                cancelar(tx);
                return 0;
            }
            PrestamoEntity p = new PrestamoEntity();
            p.setIdSocio(idSocio);
            p.setIdLibro(idLibro);
            p.setFechaPrestamo(inicio);
            p.setFechaLimite(limite);
            p.setFechaDevolucion(null);
            em.persist(p);
            em.flush();
            tx.commit();
            return p.getIdPrestamo();
        } catch (Exception ex) {
            cancelar(tx);
            registrarLog("registrarPrestamo", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Asienta la devolucion de un prestamo activo validando coherencia temporal.
     *
     * @param id identificador numerico correspondiente al prestamo a liquidar
     * @param fecha fecha real en la que el usuario efectua la devolucion YYYY-MM-DD
     * @return 1 si se devolvio correctamente o 0 si el prestamo no existe o fallo
     */
    public int devolver(int id, String fecha) {
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PrestamoEntity entidad = em.find(PrestamoEntity.class, id);
            if (entidad.getFechaDevolucion() != null) {
                cancelar(tx);
                return 0;
            }
            if (fecha.compareTo(entidad.getFechaPrestamo()) < 0) {
                cancelar(tx);
                return 0;
            }
            entidad.setFechaDevolucion(fecha);
            tx.commit();
            return 1;
        } catch (Exception ex) {
            cancelar(tx);
            registrarLog("devolver", ex);
            return 0;
        } finally {
            em.close();
        }
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
