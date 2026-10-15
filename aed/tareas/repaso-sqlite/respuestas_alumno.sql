-- Completa cada ejercicio con UNA consulta SELECT (o WITH ... SELECT).
-- No borres las lineas: -- EJERCICIO NN
-- Termina cada consulta con punto y coma.

-- EJERCICIO 01
-- Mostrar los cinco primeros productos por identificador.
SELECT id, nombre, precio
FROM Productos
ORDER BY id ASC LIMIT 5;

-- EJERCICIO 02
-- Ver los cinco vehiculos mas recientes.
SELECT matricula, marca, modelo, anio
FROM Vehiculos
ORDER BY anio DESC LIMIT 5;

-- EJERCICIO 03
-- Filtrar productos cuyo precio sea mayor de 100.
SELECT id, nombre, precio
FROM Productos
WHERE precio > 100
ORDER BY precio DESC, id DESC LIMIT 8;

-- EJERCICIO 04
-- Localizar vehiculos de la marca Toyota.
SELECT matricula, marca, modelo, anio
FROM Vehiculos
WHERE marca = 'Toyota'
ORDER BY anio DESC, matricula DESC LIMIT 8;

-- EJERCICIO 05
-- Buscar productos cuyo nombre contenga Pro.
SELECT id, nombre, precio
FROM Productos
WHERE nombre LIKE '%Pro%'
ORDER BY id ASC LIMIT 8;

-- EJERCICIO 06
-- Mostrar productos con precio comprendido entre 20 y 50.
SELECT id, nombre, precio
FROM Productos
WHERE precio BETWEEN 20 AND 50
ORDER BY precio ASC, id DESC LIMIT 8;

-- EJERCICIO 07
-- Listar marcas diferentes sin repetir.
SELECT DISTINCT marca
FROM Vehiculos
ORDER BY marca ASC;

-- EJERCICIO 08
-- Seleccionar vehiculos de dos marcas concretas.
SELECT matricula, marca, modelo, anio
FROM Vehiculos
WHERE marca IN ('Toyota', 'Ford')
ORDER BY marca ASC, anio DESC, matricula DESC LIMIT 8;

-- EJERCICIO 09
-- Contar cuantos productos existen.
SELECT COUNT(*)
FROM Productos;

-- EJERCICIO 10
-- Contar cuantos vehiculos estan registrados.
SELECT COUNT(*)
FROM Vehiculos;

-- EJERCICIO 11
-- Obtener precio minimo, maximo y medio.
SELECT MIN(precio), MAX(precio), ROUND(AVG(precio), 2)
FROM Productos;

-- EJERCICIO 12
-- Contar las marcas diferentes.
SELECT COUNT(DISTINCT marca)
FROM Vehiculos;

-- EJERCICIO 13
-- Calcular la suma de todos los precios del catalogo.
SELECT SUM(precio)
FROM Productos;

-- EJERCICIO 14
-- Contar productos con precio de al menos 50.
SELECT COUNT(*)
FROM Productos
WHERE precio >= 50;

-- EJERCICIO 15
-- Contar vehiculos de cada marca.
SELECT marca, COUNT(*)
FROM Vehiculos
GROUP BY marca
ORDER BY marca ASC;

-- EJERCICIO 16
-- Encontrar los anos de fabricacion con mas vehiculos.
SELECT anio, COUNT(*) AS cantidad
FROM Vehiculos
GROUP BY anio
ORDER BY cantidad DESC, anio DESC LIMIT 10;

-- EJERCICIO 17
-- Calcular ano mas antiguo y reciente por marca.
SELECT marca, MIN(anio), MAX(anio)
FROM Vehiculos
GROUP BY marca
ORDER BY marca ASC;

-- EJERCICIO 18
-- Contar productos por tramos de precio.
SELECT CASE
           WHEN precio < 25 THEN 'Menos de 25'
           WHEN precio < 75 THEN '25 a menos de 75'
           ELSE '75 o más' END AS tramo,
       COUNT(*)                AS cantidad
FROM Productos
GROUP BY tramo
ORDER BY cantidad ASC;

-- EJERCICIO 19
-- Filtrar marcas con al menos cinco vehiculos.
SELECT marca, COUNT(*)
FROM Vehiculos
GROUP BY marca
HAVING COUNT(*) >= 5
ORDER BY marca ASC;

-- EJERCICIO 20
-- Mostrar anos en los que hay al menos tres vehiculos.
SELECT anio, COUNT(*) AS cantidad
FROM Vehiculos
GROUP BY anio
HAVING COUNT(*) >= 3
ORDER BY cantidad DESC, anio DESC LIMIT 10;

-- EJERCICIO 21
-- Mostrar el precio medio por familia, usando la primera palabra del nombre.
SELECT SUBSTR(nombre, 1, INSTR(nombre || ' ', ' ') - 1) AS familia,
       COUNT(*)                                         AS productos,
       ROUND(AVG(precio), 2)                            AS media
FROM Productos
GROUP BY 1
ORDER BY productos DESC, familia ASC LIMIT 12;

-- EJERCICIO 22
-- Mostrar familias que tengan al menos cuatro productos y precio medio superior a 35.
SELECT SUBSTR(nombre, 1, INSTR(nombre || ' ', ' ') - 1) AS familia,
       COUNT(*)                                         AS productos,
       ROUND(AVG(precio), 2)                            AS media
FROM Productos
GROUP BY 1
HAVING COUNT(*) >= 4
   AND AVG(precio) > 35
ORDER BY media DESC;

-- EJERCICIO 23
-- Mostrar productos mas caros que la media general.
SELECT id, nombre, precio
FROM Productos
WHERE precio > (SELECT AVG(precio) FROM Productos)
ORDER BY precio DESC, id DESC LIMIT 10;

-- EJERCICIO 24
-- Listar los modelos de los vehiculos posteriores al ano 2020 agrupados por marca.
SELECT marca, COUNT(*) AS vehiculos_recientes
FROM Vehiculos
WHERE anio > 2020
GROUP BY marca
ORDER BY vehiculos_recientes DESC, marca ASC LIMIT 10;

-- EJERCICIO 25
-- Detectar los productos empatados con el precio maximo.
SELECT id, nombre, precio
FROM Productos
WHERE precio = (SELECT MAX(precio) FROM Productos);

-- EJERCICIO 26
-- Clasificar productos segun el precio usando un ranking.
SELECT id, nombre, precio, DENSE_RANK() OVER (ORDER BY precio DESC)
FROM Productos
ORDER BY precio DESC, id DESC LIMIT 10;

-- EJERCICIO 27
-- Calcular un total acumulado de precios ordenados por identificador.
SELECT id, nombre, precio, SUM(precio) OVER (ORDER BY id ASC)
FROM Productos
ORDER BY id ASC LIMIT 10;

-- EJERCICIO 28
-- Comparar cada producto con el precio medio de su familia.
SELECT id,
       nombre,
       SUBSTR(nombre, 1, INSTR(nombre || ' ', ' ') - 1)                                           AS familia,
       precio,
       ROUND(AVG(precio) OVER (PARTITION BY SUBSTR(nombre, 1, INSTR(nombre || ' ', ' ') - 1)), 2) AS media_familia
FROM Productos
ORDER BY familia ASC, precio DESC LIMIT 12;

-- EJERCICIO 29
-- Mostrar el vehiculo mas reciente de cada marca, incluyendo empates.
WITH RankingVehiculos AS (SELECT matricula,
                                 marca,
                                 modelo,
                                 anio,
                                 DENSE_RANK() OVER (PARTITION BY marca ORDER BY anio DESC) AS rn
                          FROM Vehiculos)
SELECT matricula, marca, modelo, anio
FROM RankingVehiculos
WHERE rn = 1
ORDER BY marca ASC LIMIT 10;

-- EJERCICIO 30
-- Resumir por marca el total, ano medio y antiguedad, conservando solo marcas con al menos tres vehiculos.
SELECT marca, COUNT(*), AVG(anio), MIN(anio), MAX(anio)
FROM Vehiculos
GROUP BY marca
HAVING COUNT(*) >= 3
ORDER BY marca ASC LIMIT 10;
