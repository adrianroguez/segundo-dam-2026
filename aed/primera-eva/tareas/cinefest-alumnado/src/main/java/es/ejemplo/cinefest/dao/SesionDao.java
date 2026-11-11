package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Sesion;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.EntradaEntity;
import es.ejemplo.cinefest.orm.entity.EspectadorEntity;
import es.ejemplo.cinefest.orm.entity.SalaEntity;
import es.ejemplo.cinefest.orm.entity.SesionEntity;
import es.ejemplo.cinefest.orm.mapper.SesionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de sesiones cinematográficas del festival.
 */
public class SesionDao {

    private final EntityManagerFactory emf;

    /**
     * Construye el DAO configurando la conexión con la base de datos.
     *
     * @param jdbcUrl URL JDBC para conectar con SQLite.
     */
    public SesionDao(String jdbcUrl) {
        this.emf = OrmFactory.crear(jdbcUrl);
    }

    /**
     * Obtiene la lista completa de sesiones ordenadas por fecha y hora.
     *
     * @return Lista de sesiones, o lista vacía si no hay registros o ante error.
     */
    public List<Sesion> listar() {
        EntityManager em = emf.createEntityManager();
        try {
            List<SesionEntity> entities = em.createQuery(
                    "SELECT s FROM SesionEntity s ORDER BY s.fecha ASC, s.hora ASC, s.idSesion ASC",
                    SesionEntity.class).getResultList();
            return entities.stream().map(SesionMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Busca una sesión por su identificador único.
     *
     * @param id Identificador de la sesión.
     * @return Optional con la sesión encontrada o empty si no existe o ante error.
     */
    public Optional<Sesion> buscarPorId(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            SesionEntity entity = em.find(SesionEntity.class, id);
            return Optional.ofNullable(SesionMapper.desdeEntidad(entity));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Inserta una nueva sesión en la base de datos.
     *
     * @param dato Objeto Sesion con los datos a persistir.
     * @return Identificador generado (>0) si tiene éxito, 0 en caso de fallo.
     */
    public int insertar(Sesion dato) {
        if (dato == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SesionEntity entity = SesionMapper.aEntidad(dato);
            em.persist(entity);
            tx.commit();
            dato.setIdSesion(entity.getIdSesion());
            return entity.getIdSesion() != null ? entity.getIdSesion() : 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza la información de una sesión existente.
     *
     * @param dato Sesion con los datos actualizados y su id.
     * @return 1 si se actualizó correctamente, 0 si no existe o ante error.
     */
    public int actualizar(Sesion dato) {
        if (dato == null || dato.getIdSesion() == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SesionEntity existing = em.find(SesionEntity.class, dato.getIdSesion());
            if (existing == null) {
                tx.rollback();
                return 0;
            }
            existing.setIdPelicula(dato.getIdPelicula());
            existing.setIdSala(dato.getIdSala());
            existing.setFecha(dato.getFecha());
            existing.setHora(dato.getHora());
            existing.setPrecio(dato.getPrecio());
            existing.setEstado(dato.getEstado());
            em.merge(existing);
            tx.commit();
            return 1;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Elimina una sesión por su identificador.
     *
     * @param id Identificador de la sesión a eliminar.
     * @return 1 si se eliminó con éxito, 0 si no existe o no procede por restricciones FK.
     */
    public int eliminar(int id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SesionEntity entity = em.find(SesionEntity.class, id);
            if (entity == null) {
                tx.rollback();
                return 0;
            }
            em.remove(entity);
            tx.commit();
            return 1;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Consulta sesiones con título de película, sede y sala (Reto C07).
     *
     * @return Lista con id_sesion, pelicula, sede, sala, fecha, hora ordenados por id_sesion ASC.
     */
    public List<Object[]> sesionesConDetalles() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT se.id_sesion, p.titulo AS pelicula, sd.nombre AS sede, sa.nombre AS sala, se.fecha, se.hora " +
                         "FROM sesiones se " +
                         "JOIN peliculas p ON se.id_pelicula = p.id_pelicula " +
                         "JOIN salas sa ON se.id_sala = sa.id_sala " +
                         "JOIN sedes sd ON sa.id_sede = sd.id_sede " +
                         "ORDER BY se.id_sesion ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta sesiones programadas para una fecha concreta.
     *
     * @param fecha Fecha en formato YYYY-MM-DD.
     * @return Lista con fecha, hora, id_sesion, precio ordenados por hora ASC, id_sesion ASC.
     */
    public List<Object[]> sesionesPorFecha(String fecha) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT se.fecha, se.hora, se.id_sesion, se.precio " +
                         "FROM sesiones se " +
                         "WHERE se.fecha = :fecha " +
                         "ORDER BY se.hora ASC, se.id_sesion ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql)
                    .setParameter("fecha", fecha)
                    .getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta sesiones en estado PROGRAMADA sin ventas válidas (Reto C17).
     *
     * @return Lista con id_sesion, pelicula, fecha ordenados por id_sesion ASC.
     */
    public List<Object[]> sesionesSinVentas() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT se.id_sesion, p.titulo AS pelicula, se.fecha " +
                         "FROM sesiones se " +
                         "JOIN peliculas p ON se.id_pelicula = p.id_pelicula " +
                         "LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA') " +
                         "WHERE se.estado = 'PROGRAMADA' " +
                         "GROUP BY se.id_sesion, p.titulo, se.fecha " +
                         "HAVING COUNT(e.id_entrada) = 0 " +
                         "ORDER BY se.id_sesion ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta plazas vendidas y libres por sesión (Reto C15).
     *
     * @return Lista con id_sesion, aforo, vendidas, libres ordenados por id_sesion ASC.
     */
    public List<Object[]> disponibilidadSesiones() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT se.id_sesion, sa.aforo, " +
                         "COUNT(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN 1 END) AS vendidas, " +
                         "(sa.aforo - COUNT(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN 1 END)) AS libres " +
                         "FROM sesiones se " +
                         "JOIN salas sa ON se.id_sala = sa.id_sala " +
                         "LEFT JOIN entradas e ON se.id_sesion = e.id_sesion " +
                         "GROUP BY se.id_sesion, sa.aforo " +
                         "ORDER BY se.id_sesion ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta sesiones que han agotado su aforo disponible (Reto C18).
     *
     * @return Lista con id_sesion, pelicula, aforo ordenados por id_sesion ASC.
     */
    public List<Object[]> sesionesAgotadas() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT se.id_sesion, p.titulo AS pelicula, sa.aforo " +
                         "FROM sesiones se " +
                         "JOIN peliculas p ON se.id_pelicula = p.id_pelicula " +
                         "JOIN salas sa ON se.id_sala = sa.id_sala " +
                         "LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY se.id_sesion, p.titulo, sa.aforo " +
                         "HAVING COUNT(e.id_entrada) = sa.aforo " +
                         "ORDER BY se.id_sesion ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta el top 3 de películas con mayor recaudación de entradas válidas (Reto C21).
     *
     * @return Lista con pelicula, entradas_validas, recaudacion ordenados por recaudación DESC, pelicula ASC.
     */
    public List<Object[]> rankingRecaudacion() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT p.titulo AS pelicula, COUNT(e.id_entrada) AS entradas_validas, ROUND(SUM(se.precio), 2) AS recaudacion " +
                         "FROM peliculas p " +
                         "JOIN sesiones se ON p.id_pelicula = se.id_pelicula " +
                         "JOIN entradas e ON se.id_sesion = e.id_sesion " +
                         "WHERE e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY p.id_pelicula, p.titulo " +
                         "ORDER BY SUM(se.precio) DESC, pelicula ASC " +
                         "LIMIT 3";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta la recaudación total agrupada por cada sede, incluyendo sedes sin ventas (Reto C22).
     *
     * @return Lista con sede y recaudacion ordenados por recaudación DESC, sede ASC.
     */
    public List<Object[]> ingresosPorSede() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT sd.nombre AS sede, " +
                         "ROUND(COALESCE(SUM(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN se.precio ELSE 0.0 END), 0.0), 2) AS recaudacion " +
                         "FROM sedes sd " +
                         "LEFT JOIN salas sa ON sd.id_sede = sa.id_sede " +
                         "LEFT JOIN sesiones se ON sa.id_sala = se.id_sala " +
                         "LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY sd.id_sede, sd.nombre " +
                         "ORDER BY COALESCE(SUM(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN se.precio ELSE 0.0 END), 0.0) DESC, sede ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Genera el informe diario de sesiones y ventas, incluyendo días sin ventas (Reto C25).
     *
     * @return Lista con fecha, sesiones_programadas, entradas_validas, recaudacion ordenados por fecha ASC.
     */
    public List<Object[]> informeDiario() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT se.fecha, " +
                         "COUNT(DISTINCT CASE WHEN se.estado = 'PROGRAMADA' THEN se.id_sesion END) AS sesiones_programadas, " +
                         "COUNT(e.id_entrada) AS entradas_validas, " +
                         "ROUND(COALESCE(SUM(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN se.precio ELSE 0.0 END), 0.0), 2) AS recaudacion " +
                         "FROM sesiones se " +
                         "LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY se.fecha " +
                         "ORDER BY se.fecha ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Vende una entrada de forma atómica comprobando el control de aforo y unicidad (Reto C29).
     *
     * @param idSesion Identificador de la sesión (debe estar en estado PROGRAMADA).
     * @param idEspectador Identificador del espectador (debe estar activo y sin entrada previa).
     * @param fechaCompra Fecha de la compra en formato YYYY-MM-DD.
     * @return 1 si la venta se completó con éxito, 0 si no hay aforo, los datos no son válidos o ante error.
     */
    public int venderEntrada(int idSesion, int idEspectador, String fechaCompra) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            SesionEntity sesion = em.find(SesionEntity.class, idSesion);
            if (sesion == null || !"PROGRAMADA".equalsIgnoreCase(sesion.getEstado())) {
                tx.rollback();
                return 0;
            }

            EspectadorEntity espectador = em.find(EspectadorEntity.class, idEspectador);
            if (espectador == null || espectador.getActivo() == null || espectador.getActivo() != 1) {
                tx.rollback();
                return 0;
            }

            String checkEntrada = "SELECT COUNT(e) FROM EntradaEntity e WHERE e.idSesion = :idSesion AND e.idEspectador = :idEspectador";
            Long existingCount = em.createQuery(checkEntrada, Long.class)
                    .setParameter("idSesion", idSesion)
                    .setParameter("idEspectador", idEspectador)
                    .getSingleResult();
            if (existingCount != null && existingCount > 0) {
                tx.rollback();
                return 0;
            }

            SalaEntity sala = em.find(SalaEntity.class, sesion.getIdSala());
            if (sala == null || sala.getAforo() == null) {
                tx.rollback();
                return 0;
            }

            String checkAforo = "SELECT COUNT(e) FROM EntradaEntity e WHERE e.idSesion = :idSesion AND e.estado IN ('PAGADA', 'USADA')";
            Long validTickets = em.createQuery(checkAforo, Long.class)
                    .setParameter("idSesion", idSesion)
                    .getSingleResult();
            long vendidas = (validTickets != null) ? validTickets : 0;
            if (vendidas >= sala.getAforo()) {
                tx.rollback();
                return 0;
            }

            EntradaEntity nueva = new EntradaEntity();
            nueva.setIdSesion(idSesion);
            nueva.setIdEspectador(idEspectador);
            nueva.setFechaCompra(fechaCompra);
            nueva.setEstado("PAGADA");
            em.persist(nueva);

            tx.commit();
            return 1;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

}
