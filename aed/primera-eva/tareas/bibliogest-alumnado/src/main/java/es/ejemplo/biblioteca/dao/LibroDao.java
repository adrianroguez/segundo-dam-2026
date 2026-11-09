package es.ejemplo.biblioteca.dao;

import es.ejemplo.biblioteca.modelo.Libro;
import es.ejemplo.biblioteca.orm.config.OrmFactory;
import es.ejemplo.biblioteca.orm.entity.AutorEntity;
import es.ejemplo.biblioteca.orm.entity.LibroEntity;
import es.ejemplo.biblioteca.orm.mapper.LibroMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Objeto de acceso a datos para la entidad de libros y ejemplares del catalogo.
 * Proporciona metodos de persistencia, filtros tematicos y agregaciones complejas.
 */
public class LibroDao implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(LibroDao.class.getName());
    private final EntityManagerFactory managerFactory;
    private final boolean ownFactory;

    /**
     * Inicializa una nueva instancia de LibroDao creando su propia factoría ORM.
     *
     * @param url direccion de conexion JDBC hacia la base de datos SQLite
     */
    public LibroDao(String url) {
        this.managerFactory = OrmFactory.crear(url);
        this.ownFactory = true;
    }

    /**
     * Inicializa una nueva instancia de LibroDao reutilizando una factoría existente.
     *
     * @param factory factoria de EntityManager configurada para la persistencia
     */
    public LibroDao(EntityManagerFactory factory) {
        this.managerFactory = factory;
        this.ownFactory = false;
    }

    private void anotarFallo(String ruta, Exception causa) {
        LOG.log(Level.WARNING, ruta + " no se pudo completar: " + causa.getMessage());
    }

    private void desistirTx(EntityTransaction t) {
        if (t.isActive()) {
            t.rollback();
        }
    }

    private <T> List<T> leerTodos(String jpql, Class<T> clase) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).getResultList();
        } catch (Exception ex) {
            anotarFallo("leerTodos", ex);
            return List.of();
        }
    }

    private <T> List<T> leerCon(String jpql, Class<T> clase, String p, Object v) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).setParameter(p, v).getResultList();
        } catch (Exception ex) {
            anotarFallo("leerCon", ex);
            return List.of();
        }
    }

    private <T> List<T> leerCon2(String jpql, Class<T> clase, String p1, Object v1, String p2, Object v2) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return em.createQuery(jpql, clase).setParameter(p1, v1).setParameter(p2, v2).getResultList();
        } catch (Exception ex) {
            anotarFallo("leerCon2", ex);
            return List.of();
        }
    }

    private boolean validar(Libro dato) {
        try {
            String t = dato.getTitulo(), i = dato.getIsbn(), g = dato.getGenero();
            return dato.getIdAutor() != null && dato.getEjemplares() >= 0
                    && !t.isBlank() && !i.isBlank() && !g.isBlank();
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Obtiene todos los libros catalogados ordenados alfabeticamente por su titulo.
     *
     * @return lista completa de libros registrados o lista vacia ante incidencias
     */
    public List<Libro> listar() {
        return leerTodos("SELECT l FROM LibroEntity l ORDER BY l.titulo ASC", LibroEntity.class)
                .stream().map(LibroMapper::toDomain).toList();
    }

    /**
     * Localiza un libro concreto en el catalogo a partir de su identificador primario.
     *
     * @param id identificador numerico unico del libro que se desea consultar
     * @return Optional con el libro localizado o vacio si no se encuentra en el catalogo
     */
    public Optional<Libro> buscarPorId(Integer id) {
        try (EntityManager em = managerFactory.createEntityManager()) {
            return Optional.ofNullable(em.find(LibroEntity.class, id)).map(LibroMapper::toDomain);
        } catch (Exception ex) {
            anotarFallo("buscarPorId", ex);
            return Optional.empty();
        }
    }

    /**
     * Inserta un nuevo libro en el catalogo comprobando la validez del autor foraneo.
     *
     * @param dato modelo que almacena la informacion completa del libro a persistir
     * @return clave generada positiva en caso de exito o 0 si la operacion fracasa
     */
    public int insertar(Libro dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            LibroEntity entidad = LibroMapper.toEntity(dato);
            entidad.setIdLibro(null);
            em.persist(entidad);
            em.flush();
            tx.commit();
            return entidad.getIdLibro();
        } catch (Exception ex) {
            desistirTx(tx);
            anotarFallo("insertar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Actualiza la informacion y numero de ejemplares de un libro ya registrado.
     *
     * @param dato modelo de libro con datos actualizados e identificador existente
     * @return 1 si el libro fue modificado con exito o 0 si no existe o hay error
     */
    public int actualizar(Libro dato) {
        if (!validar(dato)) {
            return 0;
        }
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            LibroEntity entidad = em.find(LibroEntity.class, dato.getIdLibro());
            entidad.setTitulo(dato.getTitulo());
            entidad.setIsbn(dato.getIsbn());
            entidad.setGenero(dato.getGenero());
            entidad.setAnioPublicacion(dato.getAnioPublicacion());
            entidad.setEjemplares(dato.getEjemplares());
            entidad.setIdAutor(dato.getIdAutor());
            tx.commit();
            return 1;
        } catch (Exception ex) {
            desistirTx(tx);
            anotarFallo("actualizar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    private boolean tienePrestamosOReservas(EntityManager em, Integer id) {
        Long total = em.createQuery(
                "SELECT COUNT(p) FROM PrestamoEntity p WHERE p.idLibro = :id", Long.class)
                .setParameter("id", id).getSingleResult()
                + em.createQuery(
                "SELECT COUNT(r) FROM ReservaEntity r WHERE r.idLibro = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        return total > 0;
    }

    /**
     * Elimina un libro del catalogo verificando que no posea prestamos ni reservas.
     *
     * @param id identificador numerico correspondiente al libro que se va a borrar
     * @return 1 si se elimino correctamente o 0 si no existe o mantiene referencias
     */
    private int eliminarEntidad(EntityManager em, EntityTransaction tx, LibroEntity e, Integer id) {
        if (e == null | tienePrestamosOReservas(em, id)) {
            desistirTx(tx);
            return 0;
        }
        em.remove(e);
        tx.commit();
        return 1;
    }

    /**
     * Elimina un libro del catalogo verificando que no posea prestamos ni reservas.
     *
     * @param id identificador numerico correspondiente al libro que se va a borrar
     * @return 1 si se elimino correctamente o 0 si no existe o mantiene referencias
     */
    public int eliminar(Integer id) {
        EntityManager em = managerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            return eliminarEntidad(em, tx, em.find(LibroEntity.class, id), id);
        } catch (Exception ex) {
            desistirTx(tx);
            anotarFallo("eliminar", ex);
            return 0;
        } finally {
            em.close();
        }
    }

    /**
     * Filtra los libros que coinciden con una categoria tematica o genero especificado.
     *
     * @param genero genero literario exacto utilizado como criterio de busqueda
     * @return lista de libros encontrados pertenecientes al genero especificado
     */
    public List<Libro> buscarPorGenero(String genero) {
        return leerCon("SELECT l FROM LibroEntity l WHERE l.genero = :genero ORDER BY l.titulo ASC",
                LibroEntity.class, "genero", genero).stream().map(LibroMapper::toDomain).toList();
    }

    /**
     * Localiza todos los libros catalogados creados por un autor especifico.
     *
     * @param idAutor identificador del autor cuyas obras se desean consultar
     * @return lista de libros escritos por el autor especificado ordenados por titulo
     */
    public List<Libro> buscarPorAutor(int idAutor) {
        return leerCon("SELECT l FROM LibroEntity l WHERE l.idAutor = :idAutor ORDER BY l.titulo ASC",
                LibroEntity.class, "idAutor", idAutor).stream().map(LibroMapper::toDomain).toList();
    }

    /**
     * Realiza una busqueda parcial de libros cuyo titulo contenga el texto buscado.
     *
     * @param texto fragmento de texto a localizar dentro de los titulos de libros
     * @return lista de libros que contienen la subcadena indicada en su titulo
     */
    public List<Libro> buscarPorTitulo(String texto) {
        return leerCon("SELECT l FROM LibroEntity l WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :t, '%')) "
                        + "ORDER BY l.titulo ASC", LibroEntity.class, "t", texto)
                .stream().map(LibroMapper::toDomain).toList();
    }

    /**
     * Recupera aquellos libros que disponen de ejemplares fisicos libres para prestamo.
     * @return lista de libros cuyos ejemplares superan los prestamos activos actuales
     */
    public List<Libro> listarDisponibles() {
        return leerTodos("SELECT l FROM LibroEntity l WHERE (l.ejemplares - "
                        + "(SELECT COUNT(p) FROM PrestamoEntity p WHERE p.idLibro = l.idLibro "
                        + "AND p.fechaDevolucion IS NULL)) > 0 ORDER BY l.titulo ASC",
                LibroEntity.class).stream().map(LibroMapper::toDomain).toList();
    }

    /**
     * Filtra libros publicados a partir de un anio determinado para un genero especifico.
     *
     * @param anio anio minimo de publicacion inclusive para considerar un libro
     * @param genero genero literario sobre el cual aplicar el filtro temporal
     * @return lista de libros publicados en o tras el anio indicado con dicho genero
     */
    public List<Libro> librosDesdeAnio(int anio, String genero) {
        return leerCon2("SELECT l FROM LibroEntity l WHERE l.anioPublicacion >= :anio AND l.genero = :gen "
                + "ORDER BY l.anioPublicacion ASC, l.titulo ASC",
                LibroEntity.class, "anio", anio, "gen", genero)
                .stream().map(LibroMapper::toDomain).toList();
    }

    /**
     * Obtiene el conjunto de todos los generos literarios distintos en el catalogo.
     *
     * @return lista alfabetica con los nombres de generos sin repeticion de valores
     */
    public List<String> generos() {
        return leerTodos("SELECT DISTINCT l.genero FROM LibroEntity l ORDER BY l.genero ASC", String.class);
    }

    /**
     * Localiza los libros que nunca han sido objeto de ningun prestamo historico.
     *
     * @return lista de libros que carecen de registros asociados en la tabla prestamos
     */
    public List<Libro> nuncaPrestados() {
        return leerTodos("SELECT l FROM LibroEntity l WHERE NOT EXISTS ("
                + "SELECT 1 FROM PrestamoEntity p WHERE p.idLibro = l.idLibro) ORDER BY l.titulo ASC",
                LibroEntity.class).stream().map(LibroMapper::toDomain).toList();
    }

    /**
     * Identifica libros con mas prestamos historicos que la media global del catalogo.
     *
     * @return lista de libros cuyo recuento individual supera la media de prestamos
     */
    public List<Libro> masPrestadosQueMedia() {
        try (EntityManager em = managerFactory.createEntityManager()) {
            Long total = em.createQuery("SELECT COUNT(l) FROM LibroEntity l", Long.class).getSingleResult();
            Long prestamos = em.createQuery("SELECT COUNT(p) FROM PrestamoEntity p", Long.class).getSingleResult();
            double media = (double) prestamos / Math.max(total, 1);
            return em.createQuery("SELECT l FROM LibroEntity l WHERE (SELECT COUNT(p) FROM PrestamoEntity p "
                            + "WHERE p.idLibro = l.idLibro) > :m ORDER BY l.titulo ASC", LibroEntity.class)
                    .setParameter("m", media).getResultList()
                    .stream().map(LibroMapper::toDomain).toList();
        } catch (Exception ex) {
            anotarFallo("masPrestadosQueMedia", ex);
            return List.of();
        }
    }

    /**
     * Lista libros que poseen reservas pendientes pero no cuentan con copias disponibles.
     *
     * @return lista de libros reservados con todos sus ejemplares actualmente prestados
     */
    public List<Libro> reservadosSinDisponibilidad() {
        return leerTodos("SELECT l FROM LibroEntity l WHERE EXISTS ("
                + "SELECT 1 FROM ReservaEntity r WHERE r.idLibro = l.idLibro AND r.estado = 'PENDIENTE') "
                + "AND (SELECT COUNT(p) FROM PrestamoEntity p WHERE p.idLibro = l.idLibro "
                + "AND p.fechaDevolucion IS NULL) >= l.ejemplares ORDER BY l.titulo ASC",
                LibroEntity.class).stream().map(LibroMapper::toDomain).toList();
    }

    /**
     * Consulta el catalogo de libros enriquecido con nombre y nacionalidad del autor.
     *
     * @return lista de arreglos con titulo de la obra, nombre del autor y su nacionalidad
     */
    public List<Object[]> librosConAutor() {
        return leerTodos("SELECT l.titulo, a.nombre, a.nacionalidad FROM LibroEntity l "
                + "JOIN l.autor a ORDER BY l.titulo ASC", Object[].class);
    }

    /**
     * Calcula la cantidad de titulos diferentes catalogados por cada genero literario.
     *
     * @return lista de arreglos conteniendo el nombre del genero y su cantidad de libros
     */
    public List<Object[]> contarPorGenero() {
        return leerTodos("SELECT l.genero, COUNT(l) FROM LibroEntity l GROUP BY l.genero ORDER BY l.genero ASC",
                Object[].class);
    }

    /**
     * Obtiene generos que superan un numero minimo especificado de prestamos historicos.
     *
     * @param minimo cantidad minima estricta de prestamos requeridos para el genero
     * @return lista de arreglos con genero y numero total de prestamos registrados
     */
    public List<Object[]> prestamosPorGeneroMinimo(long minimo) {
        return leerCon("SELECT l.genero, COUNT(p.idPrestamo) FROM LibroEntity l "
                + "JOIN PrestamoEntity p ON l.idLibro = p.idLibro "
                + "GROUP BY l.genero HAVING COUNT(p.idPrestamo) > :minimo "
                + "ORDER BY COUNT(p.idPrestamo) DESC", Object[].class, "minimo", minimo);
    }

    /**
     * Calcula para cada libro los ejemplares totales, los prestados y los disponibles.
     *
     * @return lista de arreglos con id, titulo, ejemplares totales, prestados y libres
     */
    public List<Object[]> disponibilidadPorLibro() {
        return leerTodos("SELECT l.idLibro, l.titulo, l.ejemplares, "
                + "(SELECT COUNT(p) FROM PrestamoEntity p WHERE p.idLibro = l.idLibro "
                + "AND p.fechaDevolucion IS NULL), "
                + "(l.ejemplares - (SELECT COUNT(p2) FROM PrestamoEntity p2 "
                + "WHERE p2.idLibro = l.idLibro AND p2.fechaDevolucion IS NULL)) "
                + "FROM LibroEntity l ORDER BY l.titulo ASC", Object[].class);
    }

    /**
     * Elabora un balance estadistico por genero de titulos, copias y prestamos.
     *
     * @return lista de tuplas con genero, titulos, ejemplares, prestamos e historial
     */
    public List<Object[]> estadisticasGenero() {
        return leerTodos("SELECT l.genero, COUNT(DISTINCT l.idLibro), SUM(l.ejemplares), "
                + "(SELECT COUNT(p1) FROM PrestamoEntity p1 JOIN p1.libro l1 WHERE l1.genero = l.genero), "
                + "(SELECT COUNT(p2) FROM PrestamoEntity p2 JOIN p2.libro l2 "
                + "WHERE l2.genero = l.genero AND p2.fechaDevolucion IS NULL) "
                + "FROM LibroEntity l GROUP BY l.genero ORDER BY l.genero ASC", Object[].class);
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
