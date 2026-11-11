package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Espectador;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.EspectadorEntity;
import es.ejemplo.cinefest.orm.mapper.EspectadorMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de espectadores del festival.
 */
public class EspectadorDao {

    private final EntityManagerFactory emf;

    /**
     * Construye el DAO configurando la conexión con la base de datos.
     *
     * @param jdbcUrl URL JDBC para conectar con SQLite.
     */
    public EspectadorDao(String jdbcUrl) {
        this.emf = OrmFactory.crear(jdbcUrl);
    }

    /**
     * Obtiene la lista completa de espectadores activos por antigüedad (Reto C02).
     *
     * @return Lista de espectadores activos ordenados por fecha_alta ASC, nombre ASC.
     */
    public List<Espectador> listar() {
        EntityManager em = emf.createEntityManager();
        try {
            List<EspectadorEntity> entities = em.createQuery(
                    "SELECT e FROM EspectadorEntity e WHERE e.activo = 1 ORDER BY e.fechaAlta ASC, e.nombre ASC",
                    EspectadorEntity.class).getResultList();
            return entities.stream().map(EspectadorMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Busca un espectador por su identificador único.
     *
     * @param id Identificador del espectador.
     * @return Optional con el espectador encontrado o empty si no existe o ante error.
     */
    public Optional<Espectador> buscarPorId(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            EspectadorEntity entity = em.find(EspectadorEntity.class, id);
            return Optional.ofNullable(EspectadorMapper.desdeEntidad(entity));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Inserta un nuevo espectador en la base de datos.
     *
     * @param dato Objeto Espectador con los datos a persistir.
     * @return Identificador generado (>0) si tiene éxito, 0 en caso de fallo.
     */
    public int insertar(Espectador dato) {
        if (dato == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EspectadorEntity entity = EspectadorMapper.aEntidad(dato);
            em.persist(entity);
            tx.commit();
            dato.setIdEspectador(entity.getIdEspectador());
            return entity.getIdEspectador() != null ? entity.getIdEspectador() : 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza la información de un espectador existente.
     *
     * @param dato Espectador con los datos actualizados y su id.
     * @return 1 si se actualizó correctamente, 0 si no existe o ante error.
     */
    public int actualizar(Espectador dato) {
        if (dato == null || dato.getIdEspectador() == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EspectadorEntity existing = em.find(EspectadorEntity.class, dato.getIdEspectador());
            if (existing == null) {
                tx.rollback();
                return 0;
            }
            existing.setNombre(dato.getNombre());
            existing.setEmail(dato.getEmail());
            existing.setFechaAlta(dato.getFechaAlta());
            existing.setActivo(dato.getActivo());
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
     * Elimina un espectador por su identificador.
     *
     * @param id Identificador del espectador a eliminar.
     * @return 1 si se eliminó con éxito, 0 si no existe o no procede por restricciones.
     */
    public int eliminar(int id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EspectadorEntity entity = em.find(EspectadorEntity.class, id);
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
     * Busca un espectador por su dirección de correo electrónico única.
     *
     * @param email Correo electrónico a buscar.
     * @return Optional con el espectador o empty si no existe o ante error.
     */
    public Optional<Espectador> buscarPorEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM EspectadorEntity e WHERE e.email = :email";
            List<EspectadorEntity> list = em.createQuery(jpql, EspectadorEntity.class)
                    .setParameter("email", email)
                    .getResultList();
            if (list.isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(EspectadorMapper.desdeEntidad(list.get(0)));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta los espectadores que nunca han comprado entradas (Reto C09).
     *
     * @return Lista de espectadores sin compras ordenados por nombre ASC.
     */
    public List<Espectador> espectadoresSinEntradas() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT esp FROM EspectadorEntity esp WHERE esp.idEspectador NOT IN " +
                          "(SELECT ent.idEspectador FROM EntradaEntity ent) ORDER BY esp.nombre ASC";
            List<EspectadorEntity> entities = em.createQuery(jpql, EspectadorEntity.class).getResultList();
            return entities.stream().map(EspectadorMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Genera el ranking de espectadores por número de entradas no canceladas (Reto C13).
     *
     * @return Lista con espectador y total_entradas, ordenados por total_entradas DESC, espectador ASC.
     */
    public List<Object[]> rankingEspectadores() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT esp.nombre AS espectador, COUNT(e.id_entrada) AS total_entradas " +
                         "FROM espectadores esp " +
                         "LEFT JOIN entradas e ON esp.id_espectador = e.id_espectador AND e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY esp.id_espectador, esp.nombre " +
                         "ORDER BY total_entradas DESC, espectador ASC";
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
     * Consulta espectadores que tienen entradas válidas en un número mínimo de sedes distintas (Reto C20).
     *
     * @param minimo Número mínimo de sedes distintas requeridas.
     * @return Lista con espectador y sedes_distintas, ordenados por sedes_distintas DESC, espectador ASC.
     */
    public List<Object[]> espectadoresVariasSedes(int minimo) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT esp.nombre AS espectador, COUNT(DISTINCT sa.id_sede) AS sedes_distintas " +
                         "FROM espectadores esp " +
                         "JOIN entradas e ON esp.id_espectador = e.id_espectador " +
                         "JOIN sesiones se ON e.id_sesion = se.id_sesion " +
                         "JOIN salas sa ON se.id_sala = sa.id_sala " +
                         "WHERE e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY esp.id_espectador, esp.nombre " +
                         "HAVING COUNT(DISTINCT sa.id_sede) >= :minimo " +
                         "ORDER BY sedes_distintas DESC, espectador ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql)
                    .setParameter("minimo", minimo)
                    .getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

}
