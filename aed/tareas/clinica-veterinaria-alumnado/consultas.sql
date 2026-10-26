-- 1. Todos los clientes alfabeticamente
SELECT * FROM clientes ORDER BY nombre ASC;

-- 2. Mascotas de una especie
SELECT * FROM mascotas WHERE especie = 'Perro' ORDER BY id ASC;

-- 3. Mascotas de un cliente
SELECT * FROM mascotas WHERE id_cliente = 1 ORDER BY id ASC;

-- 4. Veterinarios de una especialidad
SELECT * FROM veterinarios WHERE especialidad = 'Medicina general' ORDER BY tarifa_hora ASC;

-- 5. Consultas programadas
SELECT * FROM consultas WHERE estado = 'PROGRAMADA' ORDER BY id ASC;

-- 6a. Mascota y propietario con WHERE
SELECT m.id, m.nombre AS mascota, m.especie, c.nombre AS propietario, c.telefono
FROM mascotas m, clientes c
WHERE m.id_cliente = c.id
ORDER BY m.id ASC;

-- 6b. Mascota y propietario con INNER JOIN
SELECT m.id, m.nombre AS mascota, m.especie, c.nombre AS propietario, c.telefono
FROM mascotas m
INNER JOIN clientes c ON m.id_cliente = c.id
ORDER BY m.id ASC;

-- 7. Consultas, mascota, propietario y veterinario
SELECT con.id AS id_consulta, con.fecha, con.estado, m.nombre AS mascota, cli.nombre AS propietario, v.nombre AS veterinario
FROM consultas con
INNER JOIN mascotas m ON con.id_mascota = m.id
INNER JOIN clientes cli ON m.id_cliente = cli.id
INNER JOIN veterinarios v ON con.id_veterinario = v.id
ORDER BY con.id ASC;

-- 8. Cantidad de mascotas por cliente, incluidos los de cero mascotas
SELECT c.id, c.nombre, COUNT(m.id) AS total_mascotas
FROM clientes c
LEFT JOIN mascotas m ON c.id = m.id_cliente
GROUP BY c.id, c.nombre
ORDER BY c.id ASC;

-- 9. Importe tratamientos, incluyendo consultas sin tratamientos
SELECT c.id AS id_consulta, COALESCE(SUM(t.cantidad * t.precio_unitario), 0) AS total_tratamientos
FROM consultas c
LEFT JOIN tratamientos t ON c.id = t.id_consulta
GROUP BY c.id
ORDER BY c.id ASC;

-- 10. Consultas sin tratamientos
SELECT c.id, c.fecha, c.estado
FROM consultas c
LEFT JOIN tratamientos t ON c.id = t.id_consulta
WHERE t.id IS NULL
ORDER BY c.id ASC;
