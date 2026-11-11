package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Pelicula;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.PeliculaEntity;
import es.ejemplo.cinefest.orm.mapper.PeliculaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de películas participantes en el festival.
 */
public class PeliculaDao {

    private final EntityManagerFactory emf;

    /**
     * Construye el DAO configurando la conexión con la base de datos.
     *
     * @param jdbcUrl URL JDBC para conectar con SQLite.
     */
    public PeliculaDao(String jdbcUrl) {
        this.emf = OrmFactory.crear(jdbcUrl);
    }

    /**
     * Obtiene la lista completa de películas ordenadas por título (Reto C01).
     *
     * @return Lista de películas, o lista vacía si no hay registros o ante error.
     */
    public List<Pelicula> listar() {
        EntityManager em = emf.createEntityManager();
        try {
            List<PeliculaEntity> entities = em.createQuery("SELECT p FROM PeliculaEntity p ORDER BY p.titulo ASC", PeliculaEntity.class)
                    .getResultList();
            return entities.stream().map(PeliculaMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Busca una película por su identificador único.
     *
     * @param id Identificador de la película.
     * @return Optional con la película encontrada o empty si no existe o ante error.
     */
    public Optional<Pelicula> buscarPorId(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            PeliculaEntity entity = em.find(PeliculaEntity.class, id);
            return Optional.ofNullable(PeliculaMapper.desdeEntidad(entity));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Inserta una nueva película en la base de datos.
     *
     * @param dato Objeto Pelicula con los datos a persistir.
     * @return Identificador generado (>0) si tiene éxito, 0 en caso de fallo.
     */
    public int insertar(Pelicula dato) {
        if (dato == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PeliculaEntity entity = PeliculaMapper.aEntidad(dato);
            em.persist(entity);
            tx.commit();
            dato.setIdPelicula(entity.getIdPelicula());
            return entity.getIdPelicula() != null ? entity.getIdPelicula() : 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza la información de una película existente.
     *
     * @param dato Pelicula con los datos actualizados y su id.
     * @return 1 si se actualizó correctamente, 0 si no existe o ante error.
     */
    public int actualizar(Pelicula dato) {
        if (dato == null || dato.getIdPelicula() == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PeliculaEntity existing = em.find(PeliculaEntity.class, dato.getIdPelicula());
            if (existing == null) {
                tx.rollback();
                return 0;
            }
            existing.setTitulo(dato.getTitulo());
            existing.setGenero(dato.getGenero());
            existing.setDuracionMin(dato.getDuracionMin());
            existing.setPais(dato.getPais());
            existing.setAnio(dato.getAnio());
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
     * Elimina una película por su identificador.
     *
     * @param id Identificador de la película a eliminar.
     * @return 1 si se eliminó con éxito, 0 si no existe o no procede por restricciones FK.
     */
    public int eliminar(int id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PeliculaEntity entity = em.find(PeliculaEntity.class, id);
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
     * Busca películas por su género (Reto C03).
     *
     * @param genero Género cinematográfico a filtrar.
     * @return Lista de películas coincidentes ordenadas por título ASC.
     */
    public List<Pelicula> buscarPorGenero(String genero) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT p FROM PeliculaEntity p WHERE p.genero = :genero ORDER BY p.titulo ASC";
            List<PeliculaEntity> entities = em.createQuery(jpql, PeliculaEntity.class)
                    .setParameter("genero", genero)
                    .getResultList();
            return entities.stream().map(PeliculaMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene películas estrenadas a partir de un año determinado.
     *
     * @param anio Año de corte mínimo.
     * @return Lista de películas ordenadas por año y título ASC.
     */
    public List<Pelicula> peliculasDesdeAnio(int anio) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT p FROM PeliculaEntity p WHERE p.anio >= :anio ORDER BY p.anio ASC, p.titulo ASC";
            List<PeliculaEntity> entities = em.createQuery(jpql, PeliculaEntity.class)
                    .setParameter("anio", anio)
                    .getResultList();
            return entities.stream().map(PeliculaMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Consulta las películas que no tienen ninguna sesión asignada (Reto C16).
     *
     * @return Lista de películas sin sesiones ordenadas por título ASC.
     */
    public List<Pelicula> peliculasSinSesiones() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT p FROM PeliculaEntity p WHERE p.idPelicula NOT IN " +
                          "(SELECT se.idPelicula FROM SesionEntity se) ORDER BY p.titulo ASC";
            List<PeliculaEntity> entities = em.createQuery(jpql, PeliculaEntity.class).getResultList();
            return entities.stream().map(PeliculaMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene la media y cantidad de valoraciones por película, incluyendo no valoradas (Reto C26).
     *
     * @return Lista con pelicula, numero_valoraciones, media, ordenados por película ASC.
     */
    public List<Object[]> peliculasConMediaValoraciones() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT p.titulo AS pelicula, " +
                         "COUNT(v.id_valoracion) AS numero_valoraciones, " +
                         "CASE WHEN COUNT(v.id_valoracion) > 0 THEN ROUND(AVG(v.puntuacion), 2) ELSE NULL END AS media " +
                         "FROM peliculas p " +
                         "LEFT JOIN valoraciones v ON p.id_pelicula = v.id_pelicula " +
                         "GROUP BY p.id_pelicula, p.titulo " +
                         "ORDER BY pelicula ASC";
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
     * Obtiene las películas con un número mínimo de valoraciones ordenadas por mejor media (Reto C14).
     *
     * @param minimo Cantidad mínima de valoraciones requeridas.
     * @return Lista con pelicula, numero_valoraciones, media, ordenados por media DESC, pelicula ASC.
     */
    public List<Object[]> mejorValoradas(int minimo) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT p.titulo AS pelicula, " +
                         "COUNT(v.id_valoracion) AS numero_valoraciones, " +
                         "ROUND(AVG(v.puntuacion), 2) AS media " +
                         "FROM peliculas p " +
                         "JOIN valoraciones v ON p.id_pelicula = v.id_pelicula " +
                         "GROUP BY p.id_pelicula, p.titulo " +
                         "HAVING COUNT(v.id_valoracion) >= :minimo " +
                         "ORDER BY media DESC, pelicula ASC";
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

    /**
     * Consulta películas con más ventas válidas que la media de todas las películas (Reto C19).
     *
     * @return Lista con pelicula y entradas_validas, ordenados por ventas DESC, pelicula ASC.
     */
    public List<Object[]> masVendidasQueMedia() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT p.titulo AS pelicula, COUNT(e.id_entrada) AS entradas_validas " +
                         "FROM peliculas p " +
                         "LEFT JOIN sesiones se ON p.id_pelicula = se.id_pelicula " +
                         "LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY p.id_pelicula, p.titulo " +
                         "HAVING COUNT(e.id_entrada) > ( " +
                         "    SELECT COUNT(e2.id_entrada) * 1.0 / (SELECT COUNT(*) FROM peliculas) " +
                         "    FROM entradas e2 " +
                         "    WHERE e2.estado IN ('PAGADA', 'USADA') " +
                         ") " +
                         "ORDER BY entradas_validas DESC, pelicula ASC";
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
     * Genera estadísticas de títulos, sesiones y ventas válidas agrupadas por género (Reto C27).
     *
     * @return Lista con genero, titulos, sesiones, entradas_validas ordenados por género ASC.
     */
    public List<Object[]> estadisticasGenero() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT p.genero, " +
                         "COUNT(DISTINCT p.id_pelicula) AS titulos, " +
                         "COUNT(DISTINCT se.id_sesion) AS sesiones, " +
                         "COUNT(e.id_entrada) AS entradas_validas " +
                         "FROM peliculas p " +
                         "LEFT JOIN sesiones se ON p.id_pelicula = se.id_pelicula " +
                         "LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY p.genero " +
                         "ORDER BY p.genero ASC";
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
