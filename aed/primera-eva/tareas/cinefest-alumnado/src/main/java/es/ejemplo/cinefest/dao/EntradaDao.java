package es.ejemplo.cinefest.dao;

import es.ejemplo.cinefest.modelo.Entrada;
import es.ejemplo.cinefest.orm.config.OrmFactory;
import es.ejemplo.cinefest.orm.entity.EntradaEntity;
import es.ejemplo.cinefest.orm.mapper.EntradaMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.Optional;

/**
 * DAO para la gestión de entradas del festival.
 */
public class EntradaDao {

    private final EntityManagerFactory emf;

    /**
     * Construye el DAO configurando la conexión con la base de datos.
     *
     * @param jdbcUrl URL JDBC para conectar con SQLite.
     */
    public EntradaDao(String jdbcUrl) {
        this.emf = OrmFactory.crear(jdbcUrl);
    }

    /**
     * Obtiene la lista completa de entradas registradas ordenadas por id.
     *
     * @return Lista de entradas, o lista vacía si no hay registros o ante error.
     */
    public List<Entrada> listar() {
        EntityManager em = emf.createEntityManager();
        try {
            List<EntradaEntity> entities = em.createQuery("SELECT e FROM EntradaEntity e ORDER BY e.idEntrada ASC", EntradaEntity.class)
                    .getResultList();
            return entities.stream().map(EntradaMapper::desdeEntidad).toList();
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Busca una entrada por su identificador único.
     *
     * @param id Identificador de la entrada.
     * @return Optional con la entrada encontrada o empty si no existe o ante error.
     */
    public Optional<Entrada> buscarPorId(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            EntradaEntity entity = em.find(EntradaEntity.class, id);
            return Optional.ofNullable(EntradaMapper.desdeEntidad(entity));
        } catch (Exception e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    /**
     * Inserta una nueva entrada en la base de datos.
     *
     * @param dato Objeto Entrada con los datos a persistir.
     * @return Identificador generado (>0) si tiene éxito, 0 en caso de fallo.
     */
    public int insertar(Entrada dato) {
        if (dato == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EntradaEntity entity = EntradaMapper.aEntidad(dato);
            em.persist(entity);
            tx.commit();
            dato.setIdEntrada(entity.getIdEntrada());
            return entity.getIdEntrada() != null ? entity.getIdEntrada() : 0;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza los datos de una entrada existente.
     *
     * @param dato Entrada con los datos actualizados y su id.
     * @return 1 si se actualizó correctamente, 0 si no existe o ante error.
     */
    public int actualizar(Entrada dato) {
        if (dato == null || dato.getIdEntrada() == null) return 0;
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EntradaEntity existing = em.find(EntradaEntity.class, dato.getIdEntrada());
            if (existing == null) {
                tx.rollback();
                return 0;
            }
            existing.setIdSesion(dato.getIdSesion());
            existing.setIdEspectador(dato.getIdEspectador());
            existing.setFechaCompra(dato.getFechaCompra());
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
     * Elimina una entrada por su identificador.
     *
     * @param id Identificador de la entrada a eliminar.
     * @return 1 si se eliminó con éxito, 0 si no existe o ante error.
     */
    public int eliminar(int id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EntradaEntity entity = em.find(EntradaEntity.class, id);
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
     * Consulta entradas con datos del espectador, película y estado (Reto C08).
     *
     * @return Lista con id_entrada, espectador, pelicula, estado ordenados por id_entrada ASC.
     */
    public List<Object[]> entradasConDetalles() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT e.id_entrada, esp.nombre AS espectador, p.titulo AS pelicula, e.estado " +
                         "FROM entradas e " +
                         "JOIN espectadores esp ON e.id_espectador = esp.id_espectador " +
                         "JOIN sesiones se ON e.id_sesion = se.id_sesion " +
                         "JOIN peliculas p ON se.id_pelicula = p.id_pelicula " +
                         "ORDER BY e.id_entrada ASC";
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
     * Consulta el historial de compras de un espectador (Reto C24).
     *
     * @param idEspectador Identificador del espectador.
     * @return Lista con pelicula, fecha_sesion, estado_entrada, precio ordenados por fecha_sesion ASC, pelicula ASC.
     */
    public List<Object[]> entradasPorEspectador(int idEspectador) {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT p.titulo AS pelicula, se.fecha AS fecha_sesion, e.estado AS estado_entrada, se.precio " +
                         "FROM entradas e " +
                         "JOIN sesiones se ON e.id_sesion = se.id_sesion " +
                         "JOIN peliculas p ON se.id_pelicula = p.id_pelicula " +
                         "WHERE e.id_espectador = :idEspectador " +
                         "ORDER BY fecha_sesion ASC, pelicula ASC";
            @SuppressWarnings("unchecked")
            List<Object[]> list = em.createNativeQuery(sql)
                    .setParameter("idEspectador", idEspectador)
                    .getResultList();
            return list;
        } catch (Exception e) {
            return List.of();
        } finally {
            em.close();
        }
    }

    /**
     * Obtiene el desglose de ventas diarias excluyendo cancelaciones (Reto C23).
     *
     * @return Lista con fecha_compra, entradas_validas, total ordenados por fecha_compra ASC.
     */
    public List<Object[]> ventasPorDia() {
        EntityManager em = emf.createEntityManager();
        try {
            String sql = "SELECT e.fecha_compra, COUNT(e.id_entrada) AS entradas_validas, ROUND(SUM(se.precio), 2) AS total " +
                         "FROM entradas e " +
                         "JOIN sesiones se ON e.id_sesion = se.id_sesion " +
                         "WHERE e.estado IN ('PAGADA', 'USADA') " +
                         "GROUP BY e.fecha_compra " +
                         "ORDER BY e.fecha_compra ASC";
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
     * Realiza la cancelación transaccional de una entrada liberando su plaza (Reto C30).
     * No borra el registro físico; actualiza el estado a CANCELADA.
     *
     * @param idEntrada Identificador de la entrada a cancelar.
     * @return 1 si se canceló con éxito, 0 si no existe, ya estaba cancelada o ante fallo.
     */
    public int cancelarEntrada(int idEntrada) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EntradaEntity entrada = em.find(EntradaEntity.class, idEntrada);
            if (entrada == null || "CANCELADA".equalsIgnoreCase(entrada.getEstado())) {
                tx.rollback();
                return 0;
            }
            entrada.setEstado("CANCELADA");
            em.merge(entrada);
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
