-- ============================================================================
-- ACTIVIDADES SQL: 25 RETOS DE DIFICULTAD PROGRESIVA - BIBLIOGEST
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Nivel 1 — Selección, filtros y ordenación
-- ----------------------------------------------------------------------------

-- 01. Libros ordenados por título.
SELECT id_libro, titulo, isbn, genero, anio_publicacion, ejemplares, id_autor
FROM libros
ORDER BY titulo ASC;

-- 02. Socios activos ordenados por fecha de alta descendente.
SELECT id_socio, nombre, email, telefono, fecha_alta, activo
FROM socios
WHERE activo = 1
ORDER BY fecha_alta DESC;

-- 03. Libros cuyo título contiene el texto indicado (ejemplo: 'prueba').
SELECT id_libro, titulo, isbn, genero, anio_publicacion, ejemplares, id_autor
FROM libros
WHERE LOWER(titulo) LIKE LOWER('%' || :texto || '%')
ORDER BY titulo ASC;

-- 04. Libros publicados desde un año concreto y de un género determinado.
SELECT id_libro, titulo, isbn, genero, anio_publicacion, ejemplares, id_autor
FROM libros
WHERE anio_publicacion >= :anio AND genero = :genero
ORDER BY anio_publicacion ASC, titulo ASC;

-- 05. Géneros distintos registrados en el catálogo.
SELECT DISTINCT genero
FROM libros
ORDER BY genero ASC;

-- ----------------------------------------------------------------------------
-- Nivel 2 — Relaciones entre tablas
-- ----------------------------------------------------------------------------

-- 06. Libros con nombre y nacionalidad del autor.
SELECT l.titulo, a.nombre AS autor, a.nacionalidad
FROM libros l
JOIN autores a ON l.id_autor = a.id_autor
ORDER BY l.titulo ASC;

-- 07. Préstamos con socio, libro y fechas.
SELECT p.id_prestamo, s.nombre AS socio, l.titulo AS libro,
       p.fecha_prestamo, p.fecha_limite, p.fecha_devolucion
FROM prestamos p
JOIN socios s ON p.id_socio = s.id_socio
JOIN libros l ON p.id_libro = l.id_libro
ORDER BY p.fecha_prestamo DESC;

-- 08. Préstamos activos de un socio.
SELECT id_prestamo, id_socio, id_libro, fecha_prestamo, fecha_limite, fecha_devolucion
FROM prestamos
WHERE id_socio = :idSocio
ORDER BY fecha_prestamo DESC;

-- 09. Socios que nunca han realizado préstamos.
SELECT s.id_socio, s.nombre, s.email, s.telefono, s.fecha_alta, s.activo
FROM socios s
WHERE NOT EXISTS (
    SELECT 1 FROM prestamos p WHERE p.id_socio = s.id_socio
)
ORDER BY s.nombre ASC;

-- 10. Reservas pendientes con nombre del socio, título y autor.
SELECT r.id_reserva, s.nombre AS socio, l.titulo AS libro, a.nombre AS autor, r.fecha_reserva
FROM reservas r
JOIN socios s ON r.id_socio = s.id_socio
JOIN libros l ON r.id_libro = l.id_libro
JOIN autores a ON l.id_autor = a.id_autor
WHERE r.estado = 'PENDIENTE'
ORDER BY r.fecha_reserva ASC;

-- ----------------------------------------------------------------------------
-- Nivel 3 — Agregaciones y estadísticas
-- ----------------------------------------------------------------------------

-- 11. Cantidad de libros por género.
SELECT genero, COUNT(*) AS cantidad_libros
FROM libros
GROUP BY genero
ORDER BY genero ASC;

-- 12. Cantidad de libros por autor, incluyendo autores sin libros.
SELECT a.id_autor, a.nombre, COUNT(l.id_libro) AS cantidad_libros
FROM autores a
LEFT JOIN libros l ON a.id_autor = l.id_autor
GROUP BY a.id_autor, a.nombre
ORDER BY a.nombre ASC;

-- 13. Cinco socios con más préstamos.
SELECT s.id_socio, s.nombre, COUNT(p.id_prestamo) AS total_prestamos
FROM socios s
JOIN prestamos p ON s.id_socio = p.id_socio
GROUP BY s.id_socio, s.nombre
ORDER BY total_prestamos DESC
LIMIT :limite;

-- 14. Géneros con más de cinco préstamos históricos (o mínimo indicado).
SELECT l.genero, COUNT(p.id_prestamo) AS total_prestamos
FROM libros l
JOIN prestamos p ON l.id_libro = p.id_libro
GROUP BY l.genero
HAVING COUNT(p.id_prestamo) > :minimo
ORDER BY total_prestamos DESC;

-- 15. Por libro: ejemplares totales, prestados actualmente y disponibles.
SELECT l.id_libro,
       l.titulo,
       l.ejemplares AS ejemplares_totales,
       (SELECT COUNT(*) FROM prestamos p
        WHERE p.id_libro = l.id_libro AND p.fecha_devolucion IS NULL) AS prestados_actualmente,
       (l.ejemplares - (SELECT COUNT(*) FROM prestamos p
                        WHERE p.id_libro = l.id_libro AND p.fecha_devolucion IS NULL)) AS disponibles
FROM libros l
ORDER BY l.titulo ASC;

-- ----------------------------------------------------------------------------
-- Nivel 4 — Subconsultas y casos especiales
-- ----------------------------------------------------------------------------

-- 16. Libros que nunca se han prestado.
SELECT l.id_libro, l.titulo, l.isbn, l.genero, l.anio_publicacion, l.ejemplares, l.id_autor
FROM libros l
WHERE NOT EXISTS (
    SELECT 1 FROM prestamos p WHERE p.id_libro = l.id_libro
)
ORDER BY l.titulo ASC;

-- 17. Socios con préstamos vencidos en una fecha de referencia.
SELECT DISTINCT s.id_socio, s.nombre, s.email, s.telefono, s.fecha_alta, s.activo
FROM socios s
JOIN prestamos p ON s.id_socio = p.id_socio
WHERE p.fecha_devolucion IS NULL AND p.fecha_limite < :fecha
ORDER BY s.nombre ASC;

-- 18. Libros con más préstamos históricos que la media del catálogo.
SELECT l.id_libro, l.titulo, l.isbn, l.genero, l.anio_publicacion, l.ejemplares, l.id_autor
FROM libros l
WHERE (
    SELECT COUNT(*) FROM prestamos p WHERE p.id_libro = l.id_libro
) > (
    SELECT CAST(COUNT(*) AS REAL) / (SELECT COUNT(*) FROM libros)
    FROM prestamos
)
ORDER BY l.titulo ASC;

-- 19. Autores cuyos libros han sido prestados por al menos tres socios distintos.
SELECT a.id_autor, a.nombre, a.nacionalidad, a.anio_nacimiento
FROM autores a
JOIN libros l ON a.id_autor = l.id_autor
JOIN prestamos p ON l.id_libro = p.id_libro
GROUP BY a.id_autor, a.nombre, a.nacionalidad, a.anio_nacimiento
HAVING COUNT(DISTINCT p.id_socio) >= 3
ORDER BY a.nombre ASC;

-- 20. Libros con reservas pendientes que no tienen ejemplares disponibles.
SELECT l.id_libro, l.titulo, l.isbn, l.genero, l.anio_publicacion, l.ejemplares, l.id_autor
FROM libros l
WHERE EXISTS (
    SELECT 1 FROM reservas r WHERE r.id_libro = l.id_libro AND r.estado = 'PENDIENTE'
)
AND (
    SELECT COUNT(*) FROM prestamos p WHERE p.id_libro = l.id_libro AND p.fecha_devolucion IS NULL
) >= l.ejemplares
ORDER BY l.titulo ASC;

-- ----------------------------------------------------------------------------
-- Nivel 5 — Informes y transacciones
-- ----------------------------------------------------------------------------

-- 21. Ranking de los tres libros más prestados de un mes indicado, con autor y cantidad.
SELECT l.titulo, a.nombre AS autor, COUNT(p.id_prestamo) AS cantidad_prestamos
FROM prestamos p
JOIN libros l ON p.id_libro = l.id_libro
JOIN autores a ON l.id_autor = a.id_autor
WHERE p.fecha_prestamo LIKE :anioMes || '%'
GROUP BY l.id_libro, l.titulo, a.nombre
ORDER BY cantidad_prestamos DESC
LIMIT :limite;

-- 22. Historial completo de préstamos de un socio, indicando devoluciones y retrasos.
SELECT p.id_prestamo,
       l.titulo,
       p.fecha_prestamo,
       p.fecha_limite,
       p.fecha_devolucion,
       CASE
           WHEN p.fecha_devolucion IS NOT NULL AND p.fecha_devolucion > p.fecha_limite THEN 1
           WHEN p.fecha_devolucion IS NULL AND DATE('now') > p.fecha_limite THEN 1
           ELSE 0
       END AS con_retraso
FROM prestamos p
JOIN libros l ON p.id_libro = l.id_libro
WHERE p.id_socio = :idSocio
ORDER BY p.fecha_prestamo DESC;

-- 23. Reserva pendiente más antigua de cada libro.
SELECT r.id_libro, l.titulo, r.id_reserva, s.nombre AS socio, r.fecha_reserva
FROM reservas r
JOIN libros l ON r.id_libro = l.id_libro
JOIN socios s ON r.id_socio = s.id_socio
WHERE r.estado = 'PENDIENTE'
  AND r.fecha_reserva = (
      SELECT MIN(r2.fecha_reserva)
      FROM reservas r2
      WHERE r2.id_libro = r.id_libro AND r2.estado = 'PENDIENTE'
  )
ORDER BY r.fecha_reserva ASC;

-- 24. Estadísticas por género: títulos, ejemplares, préstamos históricos y préstamos activos.
SELECT l.genero,
       COUNT(DISTINCT l.id_libro) AS total_titulos,
       SUM(l.ejemplares) AS total_ejemplares,
       (SELECT COUNT(*) FROM prestamos p1
        JOIN libros l1 ON p1.id_libro = l1.id_libro
        WHERE l1.genero = l.genero) AS prestamos_historicos,
       (SELECT COUNT(*) FROM prestamos p2
        JOIN libros l2 ON p2.id_libro = l2.id_libro
        WHERE l2.genero = l.genero AND p2.fecha_devolucion IS NULL) AS prestamos_activos
FROM libros l
GROUP BY l.genero
ORDER BY l.genero ASC;

-- 25. Registro atómico de un préstamo:
-- Límite transaccional: BEGIN IMMEDIATE TRANSACTION ... COMMIT / ROLLBACK.
--
-- Paso 1: Iniciar transacción exclusiva para evitar carreras concurrentes:
-- BEGIN IMMEDIATE TRANSACTION;
--
-- Paso 2: Comprobar que el socio existe y está activo:
-- SELECT activo FROM socios WHERE id_socio = :idSocio;
-- -> Si activo != 1 o no existe fila, ejecutar: ROLLBACK; y devolver 0.
--
-- Paso 3: Comprobar que el libro existe y tiene ejemplares disponibles:
-- SELECT ejemplares FROM libros WHERE id_libro = :idLibro;
-- SELECT COUNT(*) FROM prestamos WHERE id_libro = :idLibro AND fecha_devolucion IS NULL;
-- -> Si activos >= ejemplares o libro no existe, ejecutar: ROLLBACK; y devolver 0.
--
-- Paso 4: Insertar el préstamo:
-- INSERT INTO prestamos (id_socio, id_libro, fecha_prestamo, fecha_limite, fecha_devolucion)
-- VALUES (:idSocio, :idLibro, :fechaPrestamo, :fechaLimite, NULL);
--
-- Paso 5: Confirmar la transacción:
-- COMMIT;
-- -> Retornar el id_prestamo generado (last_insert_rowid()).
