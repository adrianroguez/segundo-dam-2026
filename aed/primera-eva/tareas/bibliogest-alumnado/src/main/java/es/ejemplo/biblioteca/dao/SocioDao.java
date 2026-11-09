package es.ejemplo.biblioteca.dao;

import es.ejemplo.biblioteca.modelo.Socio;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import es.ejemplo.biblioteca.orm.entity.SocioEntity;
import es.ejemplo.biblioteca.orm.mapper.SocioMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Objeto de acceso a datos para la entidad de socios de la biblioteca municipal.
 * Gestiona operaciones CRUD y consultas avanzadas sobre los socios registrados.
 */
public class SocioDao implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(SocioDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Inicializa una nueva instancia de SocioDao creando su propia factoría ORM.
     *
     * @param url direccion de conexion JDBC hacia la base de datos SQLite
     */
    public SocioDao(String url) {
        this.managerFactory = OrmFactory.crear(url);
        this.ownFactory = true;
    }

    /**
     * Inicializa una nueva instancia de SocioDao reutilizando una factoría existente.
     *
     * @param factory factoria de EntityManager ya configurada para la aplicacion
     */
    public SocioDao(EntityManagerFactory factory) {
        this.managerFactory = factory;
        this.ownFactory = false;
    }

    private void registrarIncidencia(String origen, Exception e) {
        LOG.log(Level.WARNING, origen + " no se pudo completar: " + e.getMessage());
    }

    private void abortarTx(EntityTransaction t) {
        if (t.isActive()) {
            t.rollback();
        }
    }

    private <T> List<T> buscarLista(String jpql, Class<T> clase) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).getResultList();
        } catch (Exception ex) {
            registrarIncidencia("buscarLista", ex);
            return List.of();
        }
    }

    private boolean validar(Socio d) {
        try {
            return (d.getFechaAlta() != null) & (d.getActivo() != null)
                    & !d.getNombre().isBlank() & !d.getEmail().isBlank();
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Obtiene la lista completa de todos los socios registrados en el sistema.
     *
     * @return lista con todos los socios persistidos o lista vacia ante incidencias
     */
    public List<Socio> listar() {
        return buscarLista("SELECT s FROM SocioEntity s ORDER BY s.idSocio ASC", SocioEntity.class)
                .stream().map(SocioMapper::toDomain).toList();
    }

    /**
     * Localiza un socio especifico en la base de datos a partir de su identificador.
     *
     * @param id clave primaria identificativa del socio que se desea buscar
     * @return socio localizado envuelto en Optional o Optional.empty si no existe
     */
    public Optional<Socio> buscarPorId(Integer id) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(SocioEntity.class, id)).map(SocioMapper::toDomain);
        } catch (Exception ex) {
            registrarIncidencia("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta un nuevo registro de socio en la base de datos de forma transaccional.
     *
     * @param dato modelo del socio que contiene la informacion obligatoria a persistir
     * @return identificador autogenerado positivo en caso de exito o 0 si ocurre un fallo
     */
    public int insertar(Socio dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SocioEntity entidad = SocioMapper.toEntity(dato);
            entidad.setIdSocio(null);
            em.persist(entidad);
            em.flush();
            tx.commit();
            return entidad.getIdSocio();
        } catch (Exception ex) {
            abortarTx(tx);
            registrarIncidencia("insertar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza la informacion de un socio existente comprobando sus restricciones.
     *
     * @param dato modelo del socio con los valores modificados e identificador valido
     * @return 1 si la fila fue actualizada con exito o 0 si no existe o hubo un error
     */
    public int actualizar(Socio dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SocioEntity entidad = em.find(SocioEntity.class, dato.getIdSocio());
            if (entidad == null) {
                abortarTx(tx);
                return 0;
            }
            entidad.setNombre(dato.getNombre());
            entidad.setEmail(dato.getEmail());
            entidad.setTelefono(dato.getTelefono());
            entidad.setFechaAlta(dato.getFechaAlta());
            entidad.setActivo(dato.getActivo());
            tx.commit();
            return 1;
        } catch (Exception ex) {
            abortarTx(tx);
            registrarIncidencia("actualizar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    private boolean tieneVinculos(EntityManager em, Integer id) {
        Long p = em.createQuery("SELECT COUNT(p) FROM PrestamoEntity p WHERE p.idSocio = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        Long r = em.createQuery("SELECT COUNT(r) FROM ReservaEntity r WHERE r.idSocio = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        return (p + r) > 0;
    }

    /**
     * Elimina un socio de la persistencia verificando la ausencia de dependencias activas.
     *
     * @param id identificador numerico correspondiente al socio que se desea suprimir
     * @return 1 si se borro correctamente el registro o 0 si no existe o tiene vinculos
     */
    public int eliminar(Integer id) {
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SocioEntity entidad = em.find(SocioEntity.class, id);
            if (entidad == null || tieneVinculos(em, id)) {
                abortarTx(tx);
                return 0;
            }
            em.remove(entidad);
            tx.commit();
            return 1;
        } catch (Exception ex) {
            abortarTx(tx);
            registrarIncidencia("eliminar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Lista todos los socios con estado activo ordenados por su fecha de alta descendente.
     *
     * @return lista ordenada con los socios dados de alta actualmente o vacia ante error
     */
    public List<Socio> listarActivos() {
        return buscarLista("SELECT s FROM SocioEntity s WHERE s.activo = true ORDER BY s.fechaAlta DESC",
                SocioEntity.class).stream().map(SocioMapper::toDomain).toList();
    }

    /**
     * Localiza un socio registrado buscando por su direccion electronica unica.
     *
     * @param email correo electronico exacto que identifica al socio en la entidad
     * @return socio coincidente envuelto en Optional o Optional.empty si no se encuentra
     */
    public Optional<Socio> buscarPorEmail(String email) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery("SELECT s FROM SocioEntity s WHERE s.email = :email", SocioEntity.class)
                    .setParameter("email", email).getResultList()
                    .stream().findFirst().map(SocioMapper::toDomain);
        } catch (Exception ex) {
            registrarIncidencia("buscarPorEmail", ex);
            return Optional.empty();
        }
    }

    /**
     * Recupera los socios que mantienen prestamos activos vencidos para la fecha dada.
     *
     * @param fecha fecha de referencia en formato YYYY-MM-DD para verificar vencimiento
     * @return lista de socios con prestamos no devueltos cuyo plazo limite ya expiro
     */
    public List<Socio> listarConPrestamosVencidos(String fecha) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT s FROM SocioEntity s JOIN PrestamoEntity p ON s.idSocio = p.idSocio "
                            + "WHERE p.fechaDevolucion IS NULL AND p.fechaLimite < :fecha ORDER BY s.nombre ASC",
                    SocioEntity.class).setParameter("fecha", fecha).getResultList()
                    .stream().map(SocioMapper::toDomain).toList();
        } catch (Exception ex) {
            registrarIncidencia("listarConPrestamosVencidos", ex);
            return List.of();
        }
    }

    /**
     * Obtiene los socios registrados en el catalogo que nunca han efectuado prestamos.
     *
     * @return lista de socios sin ningun registro historico en la tabla de prestamos
     */
    public List<Socio> sociosSinPrestamos() {
        return buscarLista("SELECT s FROM SocioEntity s WHERE NOT EXISTS ("
                + "SELECT 1 FROM PrestamoEntity p WHERE p.idSocio = s.idSocio) ORDER BY s.nombre ASC",
                SocioEntity.class).stream().map(SocioMapper::toDomain).toList();
    }

    /**
     * Genera la clasificacion de socios ordenados por mayor numero de prestamos solicitados.
     *
     * @param limite numero maximo de posiciones que se incluiran en el ranking generado
     * @return lista de arreglos de objetos con identificador, nombre y total de prestamos
     */
    public List<Object[]> rankingSocios(int limite) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery("SELECT s.idSocio, s.nombre, COUNT(p.idPrestamo) FROM SocioEntity s "
                            + "JOIN PrestamoEntity p ON s.idSocio = p.idSocio "
                            + "GROUP BY s.idSocio, s.nombre ORDER BY COUNT(p.idPrestamo) DESC",
                    Object[].class).setMaxResults(Math.max(limite, 1)).getResultList();
        } catch (Exception ex) {
            registrarIncidencia("rankingSocios", ex);
            return List.of();
        }
    }

    /**
     * Consulta el historial detallado de prestamos de un socio con analisis de puntualidad.
     *
     * @param idSocio identificador primario del socio cuyo historial se desea obtener
     * @return lista de registros con prestamo, libro, fechas y marca de retraso o devolucion
     */
    public List<Object[]> historialSocio(int idSocio) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery("SELECT p.idPrestamo, l.titulo, p.fechaPrestamo, p.fechaLimite, p.fechaDevolucion, "
                            + "(CASE WHEN p.fechaDevolucion IS NOT NULL AND p.fechaDevolucion > p.fechaLimite THEN 1 "
                            + "WHEN p.fechaDevolucion IS NULL AND p.fechaLimite < :hoy THEN 1 ELSE 0 END) "
                            + "FROM PrestamoEntity p JOIN p.libro l WHERE p.idSocio = :idSocio "
                            + "ORDER BY p.fechaPrestamo DESC",
                    Object[].class)
                    .setParameter("idSocio", idSocio)
                    .setParameter("hoy", java.time.LocalDate.now().toString())
                    .getResultList();
        } catch (Exception ex) {
            registrarIncidencia("historialSocio", ex);
            return List.of();
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
