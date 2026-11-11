package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Sede;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.SedeEntity;
import es.ejemplo.cinefest.orm.mapper.SedeMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de sedes del festival.
 */
public class SedeDao {

    private final EntityManagerFactory emf;

    /**
     * Construye el DAO configurando la conexión con la base de datos.
     *
     * @param jdbcUrl URL JDBC para conectar con SQLite.
     */
    public SedeDao(String jdbcUrl) {
        this.emf = OrmFactory.crear(jdbcUrl);
    }

    /**
     * Obtiene la lista completa de sedes registradas ordenadas por id.
     *
     * @return Lista de sedes, o lista vacía si no hay registros o ante error.
     */
    public List<Sede> listar() {
        EntityManager em = emf.createEntityManager();
        try {
            List<SedeEntity> entities = em.createQuery("SELECT s FROM SedeEntity s ORDER BY s.idSede ASC", SedeEntity.class)
                    .getResultList();
            return entities.stream().map(SedeMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Busca una sede por su identificador único.
     *
     * @param id Identificador de la sede.
     * @return Optional con la sede encontrada o empty si no existe o ante error.
     */
    public Optional<Sede> buscarPorId(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            SedeEntity entity = em.find(SedeEntity.class, id);
            return Optional.ofNullable(SedeMapper.desdeEntidad(entity));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Inserta una nueva sede en la base de datos.
     *
     * @param dato Objeto Sede con los datos a persistir.
     * @return Identificador generado (>0) si tiene éxito, 0 en caso de fallo.
     */
    public int insertar(Sede dato) {
        if (dato == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SedeEntity entity = SedeMapper.aEntidad(dato);
            em.persist(entity);
            tx.commit();
            dato.setIdSede(entity.getIdSede());
            return entity.getIdSede() != null ? entity.getIdSede() : 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza la información de una sede existente.
     *
     * @param dato Sede con los nuevos valores y su identificador.
     * @return 1 si se actualizó correctamente, 0 si no existe o ante error.
     */
    public int actualizar(Sede dato) {
        if (dato == null || dato.getIdSede() == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SedeEntity existing = em.find(SedeEntity.class, dato.getIdSede());
            if (existing == null) {
                tx.rollback();
                return 0;
            }
            existing.setNombre(dato.getNombre());
            existing.setMunicipio(dato.getMunicipio());
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
     * Elimina una sede por su identificador.
     *
     * @param id Identificador de la sede a eliminar.
     * @return 1 si se eliminó con éxito, 0 si no existe o no procede por restricciones.
     */
    public int eliminar(int id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SedeEntity entity = em.find(SedeEntity.class, id);
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
     * Obtiene las salas asociadas a una sede específica.
     *
     * @param idSede Identificador de la sede.
     * @return Lista de registros con id_sala, nombre de la sala y aforo.
     */
    public List<Object[]> salasPorSede(int idSede) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT sa.id_sala, sa.nombre, sa.aforo FROM salas sa WHERE sa.id_sede = :idSede ORDER BY sa.nombre ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql).setParameter("idSede", idSede).getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta el número total de salas y aforo total por sede (Reto C12).
     *
     * @return Lista con sede, total_salas y aforo_total, ordenados por sede ASC.
     */
    public List<Object[]> aforoTotalPorSede() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT s.nombre AS sede, COUNT(sa.id_sala) AS total_salas, COALESCE(SUM(sa.aforo), 0) AS aforo_total " +
                         "FROM sedes s LEFT JOIN salas sa ON s.id_sede = sa.id_sede " +
                         "GROUP BY s.id_sede, s.nombre ORDER BY sede ASC";
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
