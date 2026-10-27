-- ============================================================================
-- ACTIVIDADES SQL - TALLER MECÁNICO
-- ============================================================================

-- 1. Lista los clientes ordenados por nombre.
-- Obtiene todos los clientes ordenados alfabéticamente de forma ascendente.
SELECT id_cliente, nombre, email, telefono
FROM clientes
ORDER BY nombre ASC;


-- 2. Consulta matrícula, marca, modelo y año de los vehículos de una marca elegida.
-- Filtra los vehículos cuya marca coincida exactamente con la elegida ('Seat').
SELECT matricula, marca, modelo, anio
FROM vehiculos
WHERE marca = 'Seat';


-- 3. Obtén los vehículos de un cliente utilizando su identificador.
-- Recupera los vehículos asociados a un id de cliente específico.
SELECT matricula, marca, modelo, anio, id_cliente
FROM vehiculos
WHERE id_cliente = 1;


-- 4. Lista los mecánicos de una especialidad y ordénalos por tarifa.
-- Filtra por la especialidad deseada ('Motor') y ordena de menor a mayor tarifa_hora.
SELECT id_mecanico, nombre, especialidad, tarifa_hora
FROM mecanicos
WHERE especialidad = 'Motor'
ORDER BY tarifa_hora ASC;


-- 5. Obtén las reparaciones pendientes, ordenadas por fecha.
-- Filtra reparaciones cuyo estado sea estrictamente 'PENDIENTE' y las ordena cronológicamente.
SELECT id_reparacion, matricula, id_mecanico, descripcion, fecha, horas_trabajo, estado
FROM reparaciones
WHERE estado = 'PENDIENTE'
ORDER BY fecha ASC;


-- 6. Muestra la matrícula de cada vehículo y el nombre de su propietario. Resuélvelo una vez con `WHERE` y otra con `INNER JOIN`.

-- Solución 6.A: Producto cartesiano filtrado en la cláusula WHERE
SELECT v.matricula, c.nombre AS propietario
FROM vehiculos v, clientes c
WHERE v.id_cliente = c.id_cliente;

-- Solución 6.B: Unión interna explícita con INNER JOIN y condición ON (estándar ANSI SQL)
SELECT v.matricula, c.nombre AS propietario
FROM vehiculos v
INNER JOIN clientes c ON v.id_cliente = c.id_cliente;


-- 7. Muestra las reparaciones con el nombre del cliente, la matrícula y el nombre del mecánico.
-- Combina cuatro tablas: reparaciones con vehiculos, vehiculos con clientes, y reparaciones con mecanicos.
SELECT r.id_reparacion,
       c.nombre AS nombre_cliente,
       v.matricula,
       m.nombre AS nombre_mecanico,
       r.descripcion,
       r.fecha,
       r.estado
FROM reparaciones r
INNER JOIN vehiculos v ON r.matricula = v.matricula
INNER JOIN clientes c ON v.id_cliente = c.id_cliente
INNER JOIN mecanicos m ON r.id_mecanico = m.id_mecanico;


-- 8. Cuenta los vehículos de cada cliente, incluyendo clientes sin vehículos.
-- Usa LEFT JOIN para conservar todos los clientes aunque no tengan vehículos asociados.
-- Se usa COUNT(v.matricula) en vez de COUNT(*) para que devuelva 0 si no hay vehículo.
SELECT c.id_cliente, c.nombre, COUNT(v.matricula) AS total_vehiculos
FROM clientes c
LEFT JOIN vehiculos v ON c.id_cliente = v.id_cliente
GROUP BY c.id_cliente, c.nombre;


-- 9. Calcula el coste de piezas por reparación, incluyendo las reparaciones sin piezas con coste cero.
-- LEFT JOIN garantiza que las reparaciones sin piezas aparezcan en el resultado.
-- COALESCE reemplaza el valor NULL devuelto por SUM en grupos sin filas por 0.
SELECT r.id_reparacion,
       COALESCE(SUM(p.cantidad * p.precio_unitario), 0) AS coste_total_piezas
FROM reparaciones r
LEFT JOIN piezas_reparacion p ON r.id_reparacion = p.id_reparacion
GROUP BY r.id_reparacion;


-- 10. Obtén las reparaciones que no tienen piezas asociadas.
-- Se realiza un LEFT JOIN y se filtran aquellas filas donde la clave primaria de la tabla derecha es NULL (anti-join).
SELECT r.id_reparacion, r.matricula, r.descripcion, r.fecha, r.estado
FROM reparaciones r
LEFT JOIN piezas_reparacion p ON r.id_reparacion = p.id_reparacion
WHERE p.id_pieza IS NULL;


-- ============================================================================
-- EXPLICACIÓN CONCEPTUAL DEL MODELO RELACIONAL Y OPERADORES SQL
-- ============================================================================
/*
1. Diferencia entre INNER JOIN y LEFT JOIN:
   - INNER JOIN devuelve exclusivamente aquellas filas donde existe correspondencia en ambas tablas según la condición ON. Si una fila de la tabla izquierda no tiene contraparte en la derecha (o viceversa), se descarta del resultado.
   - LEFT JOIN (o LEFT OUTER JOIN) preserva todas las filas de la tabla situada a la izquierda. Si existe coincidencia en la tabla derecha, rellena sus columnas con los datos correspondientes; si no existe coincidencia, rellena todas las columnas de la tabla derecha con valores NULL.

2. Cláusula GROUP BY:
   - Agrupa filas que comparten los mismos valores en las columnas indicadas en un único registro resumen por cada grupo único. Permite aplicar funciones de agregación sobre cada conjunto separado de datos.

3. Función SUM:
   - Función de agregación que calcula la suma aritmética de una expresión o columna numérica para todas las filas de un grupo. Si el grupo no contiene filas o todos los valores son NULL, SUM devuelve NULL.

4. Función COALESCE:
   - Recibe una lista de argumentos y devuelve el primer valor no nulo de la secuencia. Es especialmente útil junto a funciones agregadas como SUM en consultas con LEFT JOIN (por ejemplo, COALESCE(SUM(...), 0)) para transformar los NULL resultantes de la ausencia de elementos relacionados en un valor neutro por defecto como 0.
*/
