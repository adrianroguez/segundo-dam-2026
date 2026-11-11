package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Sala;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.SalaEntity;
import es.ejemplo.cinefest.orm.mapper.SalaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de salas de proyección.
 */
public class SalaDao {

    private final EntityManagerFactory emf;

    /**
     * Construye el DAO configurando la conexión con la base de datos.
     *
     * @param jdbcUrl URL JDBC para conectar con SQLite.
     */
    public SalaDao(String jdbcUrl) {
        this.emf = OrmFactory.crear(jdbcUrl);
    }

    /**
     * Obtiene la lista completa de salas ordenadas por id.
     *
     * @return Lista de salas, o lista vacía si no hay registros o ante error.
     */
    public List<Sala> listar() {
        EntityManager em = emf.createEntityManager();
        try {
            List<SalaEntity> entities = em.createQuery("SELECT s FROM SalaEntity s ORDER BY s.idSala ASC", SalaEntity.class)
                    .getResultList();
            return entities.stream().map(SalaMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Busca una sala por su identificador único.
     *
     * @param id Identificador de la sala.
     * @return Optional con la sala encontrada o empty si no existe o ante error.
     */
    public Optional<Sala> buscarPorId(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            SalaEntity entity = em.find(SalaEntity.class, id);
            return Optional.ofNullable(SalaMapper.desdeEntidad(entity));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Inserta una nueva sala en la base de datos.
     *
     * @param dato Objeto Sala con los datos a persistir.
     * @return Identificador generado (>0) si tiene éxito, 0 en caso de fallo.
     */
    public int insertar(Sala dato) {
        if (dato == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SalaEntity entity = SalaMapper.aEntidad(dato);
            em.persist(entity);
            tx.commit();
            dato.setIdSala(entity.getIdSala());
            return entity.getIdSala() != null ? entity.getIdSala() : 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza los datos de una sala existente.
     *
     * @param dato Sala con los datos actualizados y su id.
     * @return 1 si se actualizó correctamente, 0 si no existe o ante error.
     */
    public int actualizar(Sala dato) {
        if (dato == null || dato.getIdSala() == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SalaEntity existing = em.find(SalaEntity.class, dato.getIdSala());
            if (existing == null) {
                tx.rollback();
                return 0;
            }
            existing.setNombre(dato.getNombre());
            existing.setAforo(dato.getAforo());
            existing.setIdSede(dato.getIdSede());
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
     * Elimina una sala por su identificador.
     *
     * @param id Identificador de la sala a eliminar.
     * @return 1 si se eliminó con éxito, 0 si no existe o ante error.
     */
    public int eliminar(int id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SalaEntity entity = em.find(SalaEntity.class, id);
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
     * Obtiene la relación de salas con su sede y aforo (Reto C06).
     *
     * @return Lista con sede, sala, aforo, ordenados por sede, sala ASC.
     */
    public List<Object[]> salasConSede() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT s.nombre AS sede, sa.nombre AS sala, sa.aforo " +
                         "FROM salas sa JOIN sedes s ON sa.id_sede = s.id_sede " +
                         "ORDER BY sede ASC, sala ASC";
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
     * Consulta las salas que no tienen sesiones programadas (Reto C28).
     *
     * @return Lista de entidades Sala sin sesiones programadas.
     */
    public List<Sala> salasSinSesiones() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT sa FROM SalaEntity sa WHERE sa.idSala NOT IN " +
                          "(SELECT se.idSala FROM SesionEntity se WHERE se.estado = 'PROGRAMADA') " +
                          "ORDER BY sa.nombre ASC";
            List<SalaEntity> entities = em.createQuery(jpql, SalaEntity.class).getResultList();
            return entities.stream().map(SalaMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta la ocupación agregada por cada sala.
     *
     * @return Lista de registros con sede, sala, aforo y total de entradas válidas vendidas.
     */
    public List<Object[]> ocupacionPorSala() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT sd.nombre AS sede, sa.nombre AS sala, sa.aforo, " +
                         "COUNT(e.id_entrada) AS vendidas " +
                         "FROM salas sa " +
                         "JOIN sedes sd ON sa.id_sede = sd.id_sede " +
                         "LEFT JOIN sesiones se ON sa.id_sala = se.id_sala " +
                         "LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY sa.id_sala, sd.nombre, sa.nombre, sa.aforo " +
                         "ORDER BY sd.nombre ASC, sa.nombre ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

}
