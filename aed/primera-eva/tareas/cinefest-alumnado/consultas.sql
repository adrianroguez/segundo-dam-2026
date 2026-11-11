-- CINEFEST. Consultas C01 a C30.

-- C01: Películas ordenadas alfabéticamente
SELECT titulo, genero, anio
FROM peliculas
ORDER BY titulo ASC;

-- C02: Espectadores activos por antigüedad
SELECT nombre, fecha_alta
FROM espectadores
WHERE activo = 1
ORDER BY fecha_alta ASC, nombre ASC;

-- C03: Películas de un género (parámetro: genero = 'Drama')
SELECT titulo, pais, anio
FROM peliculas
WHERE genero = 'Drama'
ORDER BY titulo ASC;

-- C04: Sesiones entre dos fechas (parámetros: desde = '2026-11-13', hasta = '2026-11-14')
SELECT fecha, hora, id_sesion, precio
FROM sesiones
WHERE fecha BETWEEN '2026-11-13' AND '2026-11-14'
ORDER BY fecha ASC, hora ASC, id_sesion ASC;

-- C05: Géneros distintos
SELECT DISTINCT genero
FROM peliculas
ORDER BY genero ASC;

-- C06: Salas con su sede
SELECT s.nombre AS sede, sa.nombre AS sala, sa.aforo
FROM salas sa
JOIN sedes s ON sa.id_sede = s.id_sede
ORDER BY sede ASC, sala ASC;

-- C07: Sesiones con película, sala y sede
SELECT se.id_sesion, p.titulo AS pelicula, sd.nombre AS sede, sa.nombre AS sala, se.fecha, se.hora
FROM sesiones se
JOIN peliculas p ON se.id_pelicula = p.id_pelicula
JOIN salas sa ON se.id_sala = sa.id_sala
JOIN sedes sd ON sa.id_sede = sd.id_sede
ORDER BY se.id_sesion ASC;

-- C08: Entradas con espectador y película
SELECT e.id_entrada, esp.nombre AS espectador, p.titulo AS pelicula, e.estado
FROM entradas e
JOIN espectadores esp ON e.id_espectador = esp.id_espectador
JOIN sesiones se ON e.id_sesion = se.id_sesion
JOIN peliculas p ON se.id_pelicula = p.id_pelicula
ORDER BY e.id_entrada ASC;

-- C09: Espectadores que nunca compraron entradas
SELECT esp.nombre, esp.email
FROM espectadores esp
LEFT JOIN entradas e ON esp.id_espectador = e.id_espectador
WHERE e.id_entrada IS NULL
ORDER BY esp.nombre ASC;

-- C10: Valoraciones con datos de película y espectador
SELECT p.titulo AS pelicula, esp.nombre AS espectador, v.puntuacion
FROM valoraciones v
JOIN peliculas p ON v.id_pelicula = p.id_pelicula
JOIN espectadores esp ON v.id_espectador = esp.id_espectador
ORDER BY pelicula ASC, espectador ASC;

-- C11: Número de películas por género
SELECT genero, COUNT(*) AS total_peliculas
FROM peliculas
GROUP BY genero
ORDER BY genero ASC;

-- C12: Número de salas y aforo total por sede, incluso sin salas
SELECT s.nombre AS sede, COUNT(sa.id_sala) AS total_salas, COALESCE(SUM(sa.aforo), 0) AS aforo_total
FROM sedes s
LEFT JOIN salas sa ON s.id_sede = sa.id_sede
GROUP BY s.id_sede, s.nombre
ORDER BY sede ASC;

-- C13: Ranking de espectadores por entradas no canceladas
SELECT esp.nombre AS espectador, COUNT(e.id_entrada) AS total_entradas
FROM espectadores esp
LEFT JOIN entradas e ON esp.id_espectador = e.id_espectador AND e.estado IN ('PAGADA', 'USADA')
GROUP BY esp.id_espectador, esp.nombre
ORDER BY total_entradas DESC, espectador ASC;

-- C14: Películas con al menos dos valoraciones y su media (parámetro: minimo = 2)
SELECT p.titulo AS pelicula, COUNT(v.id_valoracion) AS numero_valoraciones, ROUND(AVG(v.puntuacion), 2) AS media
FROM peliculas p
JOIN valoraciones v ON p.id_pelicula = v.id_pelicula
GROUP BY p.id_pelicula, p.titulo
HAVING COUNT(v.id_valoracion) >= 2
ORDER BY media DESC, pelicula ASC;

-- C15: Plazas vendidas y libres por sesión
SELECT se.id_sesion, sa.aforo,
       COUNT(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN 1 END) AS vendidas,
       (sa.aforo - COUNT(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN 1 END)) AS libres
FROM sesiones se
JOIN salas sa ON se.id_sala = sa.id_sala
LEFT JOIN entradas e ON se.id_sesion = e.id_sesion
GROUP BY se.id_sesion, sa.aforo
ORDER BY se.id_sesion ASC;

-- C16: Películas sin sesiones
SELECT p.titulo
FROM peliculas p
LEFT JOIN sesiones se ON p.id_pelicula = se.id_pelicula
WHERE se.id_sesion IS NULL
ORDER BY p.titulo ASC;

-- C17: Sesiones programadas sin ventas válidas
SELECT se.id_sesion, p.titulo AS pelicula, se.fecha
FROM sesiones se
JOIN peliculas p ON se.id_pelicula = p.id_pelicula
LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA')
WHERE se.estado = 'PROGRAMADA'
GROUP BY se.id_sesion, p.titulo, se.fecha
HAVING COUNT(e.id_entrada) = 0
ORDER BY se.id_sesion ASC;

-- C18: Sesiones agotadas, sin contar cancelaciones
SELECT se.id_sesion, p.titulo AS pelicula, sa.aforo
FROM sesiones se
JOIN peliculas p ON se.id_pelicula = p.id_pelicula
JOIN salas sa ON se.id_sala = sa.id_sala
LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA')
GROUP BY se.id_sesion, p.titulo, sa.aforo
HAVING COUNT(e.id_entrada) = sa.aforo
ORDER BY se.id_sesion ASC;

-- C19: Películas con más ventas válidas que la media de las nueve películas (incluidas las de cero ventas)
SELECT p.titulo AS pelicula, COUNT(e.id_entrada) AS entradas_validas
FROM peliculas p
LEFT JOIN sesiones se ON p.id_pelicula = se.id_pelicula
LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA')
GROUP BY p.id_pelicula, p.titulo
HAVING COUNT(e.id_entrada) > (
    SELECT COUNT(e2.id_entrada) * 1.0 / (SELECT COUNT(*) FROM peliculas)
    FROM entradas e2
    WHERE e2.estado IN ('PAGADA', 'USADA')
)
ORDER BY entradas_validas DESC, pelicula ASC;

-- C20: Espectadores con entradas válidas en al menos dos sedes (parámetro: minimo = 2)
SELECT esp.nombre AS espectador, COUNT(DISTINCT sa.id_sede) AS sedes_distintas
FROM espectadores esp
JOIN entradas e ON esp.id_espectador = e.id_espectador
JOIN sesiones se ON e.id_sesion = se.id_sesion
JOIN salas sa ON se.id_sala = sa.id_sala
WHERE e.estado IN ('PAGADA', 'USADA')
GROUP BY esp.id_espectador, esp.nombre
HAVING COUNT(DISTINCT sa.id_sede) >= 2
ORDER BY sedes_distintas DESC, espectador ASC;

-- C21: Top 3 de películas por recaudación de entradas válidas (parámetro: limite = 3)
SELECT p.titulo AS pelicula, COUNT(e.id_entrada) AS entradas_validas, ROUND(SUM(se.precio), 2) AS recaudacion
FROM peliculas p
JOIN sesiones se ON p.id_pelicula = se.id_pelicula
JOIN entradas e ON se.id_sesion = e.id_sesion
WHERE e.estado IN ('PAGADA', 'USADA')
GROUP BY p.id_pelicula, p.titulo
ORDER BY SUM(se.precio) DESC, pelicula ASC
LIMIT 3;

-- C22: Recaudación por sede, incluyendo sedes sin ventas
SELECT sd.nombre AS sede,
       ROUND(COALESCE(SUM(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN se.precio ELSE 0.0 END), 0.0), 2) AS recaudacion
FROM sedes sd
LEFT JOIN salas sa ON sd.id_sede = sa.id_sede
LEFT JOIN sesiones se ON sa.id_sala = se.id_sala
LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA')
GROUP BY sd.id_sede, sd.nombre
ORDER BY COALESCE(SUM(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN se.precio ELSE 0.0 END), 0.0) DESC, sede ASC;

-- C23: Ventas diarias, excluyendo entradas canceladas
SELECT e.fecha_compra, COUNT(e.id_entrada) AS entradas_validas, ROUND(SUM(se.precio), 2) AS total
FROM entradas e
JOIN sesiones se ON e.id_sesion = se.id_sesion
WHERE e.estado IN ('PAGADA', 'USADA')
GROUP BY e.fecha_compra
ORDER BY e.fecha_compra ASC;

-- C24: Historial de entradas de un espectador (parámetro: id_espectador = 1)
SELECT p.titulo AS pelicula, se.fecha AS fecha_sesion, e.estado AS estado_entrada, se.precio
FROM entradas e
JOIN sesiones se ON e.id_sesion = se.id_sesion
JOIN peliculas p ON se.id_pelicula = p.id_pelicula
WHERE e.id_espectador = 1
ORDER BY fecha_sesion ASC, pelicula ASC;

-- C25: Informe por fecha de sesión, incluidos días sin ventas
SELECT se.fecha,
       COUNT(DISTINCT CASE WHEN se.estado = 'PROGRAMADA' THEN se.id_sesion END) AS sesiones_programadas,
       COUNT(e.id_entrada) AS entradas_validas,
       ROUND(COALESCE(SUM(CASE WHEN e.estado IN ('PAGADA', 'USADA') THEN se.precio ELSE 0.0 END), 0.0), 2) AS recaudacion
FROM sesiones se
LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA')
GROUP BY se.fecha
ORDER BY se.fecha ASC;

-- C26: Media y cantidad de valoraciones por película, incluyendo películas sin valorar
SELECT p.titulo AS pelicula,
       COUNT(v.id_valoracion) AS numero_valoraciones,
       CASE WHEN COUNT(v.id_valoracion) > 0 THEN ROUND(AVG(v.puntuacion), 2) ELSE NULL END AS media
FROM peliculas p
LEFT JOIN valoraciones v ON p.id_pelicula = v.id_pelicula
GROUP BY p.id_pelicula, p.titulo
ORDER BY pelicula ASC;

-- C27: Títulos, sesiones y ventas válidas por género
SELECT p.genero,
       COUNT(DISTINCT p.id_pelicula) AS titulos,
       COUNT(DISTINCT se.id_sesion) AS sesiones,
       COUNT(e.id_entrada) AS entradas_validas
FROM peliculas p
LEFT JOIN sesiones se ON p.id_pelicula = se.id_pelicula
LEFT JOIN entradas e ON se.id_sesion = e.id_sesion AND e.estado IN ('PAGADA', 'USADA')
GROUP BY p.genero
ORDER BY p.genero ASC;

-- C28: Salas sin sesiones programadas
SELECT sd.nombre AS sede, sa.nombre AS sala
FROM salas sa
JOIN sedes sd ON sa.id_sede = sd.id_sede
LEFT JOIN sesiones se ON sa.id_sala = se.id_sala AND se.estado = 'PROGRAMADA'
WHERE se.id_sesion IS NULL
ORDER BY sede ASC, sala ASC;

-- C29: Registro atómico de entrada con control de aforo
-- Parámetros de ejemplo: id_sesion = 1, id_espectador = 8, fecha_compra = '2026-11-10'
BEGIN IMMEDIATE TRANSACTION;

INSERT INTO entradas (id_sesion, id_espectador, fecha_compra, estado)
SELECT 1, 8, '2026-11-10', 'PAGADA'
WHERE (SELECT estado FROM sesiones WHERE id_sesion = 1) = 'PROGRAMADA'
  AND (SELECT activo FROM espectadores WHERE id_espectador = 8) = 1
  AND NOT EXISTS (SELECT 1 FROM entradas WHERE id_sesion = 1 AND id_espectador = 8)
  AND (
      (SELECT sa.aforo FROM salas sa JOIN sesiones se ON sa.id_sala = se.id_sala WHERE se.id_sesion = 1)
      >
      (SELECT COUNT(*) FROM entradas WHERE id_sesion = 1 AND estado IN ('PAGADA', 'USADA'))
  );

COMMIT;

-- C30: Cancelación de entrada y liberación de plaza
-- Parámetros de ejemplo: id_entrada = 1
BEGIN IMMEDIATE TRANSACTION;

UPDATE entradas
SET estado = 'CANCELADA'
WHERE id_entrada = 1
  AND estado != 'CANCELADA';

COMMIT;
