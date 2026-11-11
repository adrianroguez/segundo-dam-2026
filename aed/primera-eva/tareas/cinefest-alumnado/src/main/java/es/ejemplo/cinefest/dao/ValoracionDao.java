package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Valoracion;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.ValoracionEntity;
import es.ejemplo.cinefest.orm.mapper.ValoracionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de valoraciones emitidas por espectadores a películas.
 */
public class ValoracionDao {

    private final EntityManagerFactory emf;

    /**
     * Construye el DAO configurando la conexión con la base de datos.
     *
     * @param jdbcUrl URL JDBC para conectar con SQLite.
     */
    public ValoracionDao(String jdbcUrl) {
        this.emf = OrmFactory.crear(jdbcUrl);
    }

    /**
     * Obtiene la lista completa de valoraciones ordenadas por id.
     *
     * @return Lista de valoraciones, o lista vacía si no hay registros o ante error.
     */
    public List<Valoracion> listar() {
        EntityManager em = emf.createEntityManager();
        try {
            List<ValoracionEntity> entities = em.createQuery(
                    "SELECT v FROM ValoracionEntity v ORDER BY v.idValoracion ASC",
                    ValoracionEntity.class).getResultList();
            return entities.stream().map(ValoracionMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Busca una valoración por su identificador único.
     *
     * @param id Identificador de la valoración.
     * @return Optional con la valoración encontrada o empty si no existe o ante error.
     */
    public Optional<Valoracion> buscarPorId(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            ValoracionEntity entity = em.find(ValoracionEntity.class, id);
            return Optional.ofNullable(ValoracionMapper.desdeEntidad(entity));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Inserta una nueva valoración en la base de datos.
     *
     * @param dato Objeto Valoracion con los datos a persistir.
     * @return Identificador generado (>0) si tiene éxito, 0 en caso de fallo.
     */
    public int insertar(Valoracion dato) {
        if (dato == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ValoracionEntity entity = ValoracionMapper.aEntidad(dato);
            em.persist(entity);
            tx.commit();
            dato.setIdValoracion(entity.getIdValoracion());
            return entity.getIdValoracion() != null ? entity.getIdValoracion() : 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza la información de una valoración existente.
     *
     * @param dato Valoracion con los datos actualizados y su id.
     * @return 1 si se actualizó correctamente, 0 si no existe o ante error.
     */
    public int actualizar(Valoracion dato) {
        if (dato == null || dato.getIdValoracion() == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ValoracionEntity existing = em.find(ValoracionEntity.class, dato.getIdValoracion());
            if (existing == null) {
                tx.rollback();
                return 0;
            }
            existing.setIdPelicula(dato.getIdPelicula());
            existing.setIdEspectador(dato.getIdEspectador());
            existing.setPuntuacion(dato.getPuntuacion());
            existing.setComentario(dato.getComentario());
            existing.setFecha(dato.getFecha());
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
     * Elimina una valoración por su identificador.
     *
     * @param id Identificador de la valoración a eliminar.
     * @return 1 si se eliminó con éxito, 0 si no existe o ante error.
     */
    public int eliminar(int id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ValoracionEntity entity = em.find(ValoracionEntity.class, id);
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
     * Consulta valoraciones con título de película y nombre del espectador (Reto C10).
     *
     * @return Lista con pelicula, espectador, puntuacion ordenados por pelicula ASC, espectador ASC.
     */
    public List<Object[]> valoracionesConDetalles() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT p.titulo AS pelicula, esp.nombre AS espectador, v.puntuacion " +
                         "FROM valoraciones v " +
                         "JOIN peliculas p ON v.id_pelicula = p.id_pelicula " +
                         "JOIN espectadores esp ON v.id_espectador = esp.id_espectador " +
                         "ORDER BY pelicula ASC, espectador ASC";
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
     * Consulta películas que aún no han recibido ninguna valoración.
     *
     * @return Lista de registros con id_pelicula y titulo de las películas sin valoraciones.
     */
    public List<Object[]> peliculasSinValoraciones() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT p.id_pelicula, p.titulo " +
                         "FROM peliculas p " +
                         "LEFT JOIN valoraciones v ON p.id_pelicula = v.id_pelicula " +
                         "WHERE v.id_valoracion IS NULL " +
                         "ORDER BY p.titulo ASC";
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
     * Consulta valoraciones recibidas por una película concreta.
     *
     * @param idPelicula Identificador de la película.
     * @return Lista con espectador, puntuacion, comentario, fecha ordenados por fecha DESC.
     */
    public List<Object[]> valoracionesPorPelicula(int idPelicula) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT esp.nombre AS espectador, v.puntuacion, v.comentario, v.fecha " +
                         "FROM valoraciones v " +
                         "JOIN espectadores esp ON v.id_espectador = esp.id_espectador " +
                         "WHERE v.id_pelicula = :idPelicula " +
                         "ORDER BY v.fecha DESC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql)
                    .setParameter("idPelicula", idPelicula)
                    .getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta estadísticas de recuento agrupadas por cada puntuación (1 a 5).
     *
     * @return Lista con puntuacion y total de valoraciones ordenadas por puntuacion ASC.
     */
    public List<Object[]> estadisticasPuntuaciones() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT v.puntuacion, COUNT(v.id_valoracion) AS total " +
                         "FROM valoraciones v " +
                         "GROUP BY v.puntuacion " +
                         "ORDER BY v.puntuacion ASC";
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
