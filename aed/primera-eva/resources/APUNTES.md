# 📚 APUNTES — Clínica Veterinaria JDBC + SQLite

---

## 1. 🗄️ Inspeccionar la base de datos con `sqlite3` en terminal

> **IMPORTANTE:** Si `sqlite3` no está instalado: `sudo apt install sqlite3`

```bash
# Abrir la base de datos
sqlite3 datos/clinica.sqlite3

# --- Dentro de sqlite3 ---

# Ver todas las tablas
.tables

# Ver el esquema completo (CREATE TABLE de cada tabla)
.schema

# Ver el esquema de una tabla concreta
.schema clientes

# Ver las columnas (cabeceras) de una tabla
PRAGMA table_info(clientes);
PRAGMA table_info(mascotas);
PRAGMA table_info(veterinarios);
PRAGMA table_info(consultas);
PRAGMA table_info(tratamientos);

# Activar cabeceras en los resultados
.headers on
.mode column

# Ver filas de una tabla
SELECT * FROM clientes;

# Salir
.quit
```

### Resultado de `PRAGMA table_info(tabla)` — columnas que devuelve:
| cid | name | type | notnull | dflt_value | pk |
|-----|------|------|---------|------------|----|
| 0 | id | INTEGER | 0 | null | 1 |
| 1 | nombre | TEXT | 1 | null | 0 |
| … | … | … | … | … | … |

---

## 2. 🗂️ Esquema de la base de datos (`schema.sql`)

```sql
PRAGMA foreign_keys = ON;

CREATE TABLE clientes (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre    TEXT    NOT NULL,
    email     TEXT    NOT NULL UNIQUE,
    telefono  TEXT    NOT NULL
);

CREATE TABLE mascotas (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre           TEXT    NOT NULL,
    especie          TEXT    NOT NULL,
    raza             TEXT,
    fecha_nacimiento TEXT    NOT NULL,
    id_cliente       INTEGER NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT
);

CREATE TABLE veterinarios (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre       TEXT    NOT NULL,
    especialidad TEXT    NOT NULL,
    tarifa_hora  NUMERIC NOT NULL CHECK(tarifa_hora >= 0)
);

CREATE TABLE consultas (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    id_mascota     INTEGER NOT NULL REFERENCES mascotas(id)    ON DELETE RESTRICT,
    id_veterinario INTEGER NOT NULL REFERENCES veterinarios(id) ON DELETE RESTRICT,
    fecha          TEXT    NOT NULL,
    horas          NUMERIC NOT NULL CHECK(horas > 0),
    estado         TEXT    NOT NULL CHECK(estado IN ('PROGRAMADA','EN_CURSO','FINALIZADA'))
);

CREATE TABLE tratamientos (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    id_consulta     INTEGER NOT NULL REFERENCES consultas(id) ON DELETE RESTRICT,
    descripcion     TEXT    NOT NULL,
    cantidad        INTEGER NOT NULL CHECK(cantidad > 0),
    precio_unitario NUMERIC NOT NULL CHECK(precio_unitario >= 0)
);
```

---

## 3. 🏗️ Organización del proyecto (Maven)

```
clinica-veterinaria-alumnado/
├── pom.xml                          ← Dependencias Maven (sqlite-jdbc, junit-jupiter)
├── datos/
│   ├── clinica.sqlite3              ← BD de producción
│   └── clinica-test.sqlite3         ← BD para tests
├── src/
│   ├── main/
│   │   ├── java/es/ejemplo/clinica/
│   │   │   ├── infra/
│   │   │   │   └── ConexionBD.java  ← Gestión de conexión JDBC
│   │   │   ├── modelo/              ← POJOs (entidades)
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Mascota.java
│   │   │   │   ├── Veterinario.java
│   │   │   │   ├── Consulta.java
│   │   │   │   └── Tratamiento.java
│   │   │   └── dao/                 ← Patrón DAO (acceso a datos)
│   │   │       ├── IClienteDao.java      ← Interfaz CRUD
│   │   │       ├── ClienteDao.java       ← Implementación
│   │   │       ├── IMascotaDao.java
│   │   │       ├── MascotaDao.java
│   │   │       ├── IVeterinarioDao.java
│   │   │       ├── VeterinarioDao.java
│   │   │       ├── IConsultaDao.java
│   │   │       ├── ConsultaDao.java
│   │   │       ├── ITratamientoDao.java
│   │   │       ├── TratamientoDao.java
│   │   │       └── DaoException.java    ← RuntimeException personalizada
│   │   └── resources/
│   │       └── schema.sql           ← DDL de la BD principal
│   └── test/
│       ├── java/es/ejemplo/clinica/ ← Tests JUnit 5 por entidad
│       └── resources/
│           └── schema.sql           ← DDL de la BD de test
```

### Capas del patrón DAO:
```
[modelo] ──► [IXxxDao] ──► [XxxDao] ──► [ConexionBD] ──► SQLite
  POJO        interfaz    implementación  conexión JDBC    archivo .sqlite3
```

---

## 4. 🔌 Clase `ConexionBD` — bien comentada

```java
package es.ejemplo.clinica.infra;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class ConexionBD {

    // ── Atributo ────────────────────────────────────────────────────────────
    private String url;  // URL JDBC: "jdbc:sqlite:ruta/al/archivo.sqlite3"

    // ── Constructor ─────────────────────────────────────────────────────────
    /**
     * Valida la URL y que el archivo .sqlite3 existe y es legible.
     * Lanza SQLException si algo falla (protocolo incorrecto, archivo no existe…)
     */
    public ConexionBD(String url) throws SQLException {
        // 1. La URL debe empezar por "jdbc:sqlite:"
        if (url == null || !url.startsWith("jdbc:sqlite:")) {
            throw new SQLException("URL no valida o protocolo no soportado: " + url);
        }
        // 2. Extrae la ruta real (quita el prefijo JDBC)
        String nuevaURL = url.substring("jdbc:sqlite:".length());
        this.url = url;

        // 3. Si NO es BD en memoria ni URI file:, comprueba que el archivo exista
        if (!":memory:".equals(nuevaURL) && !nuevaURL.startsWith("file:")) {
            Path path = Path.of(nuevaURL);
            if (!Files.exists(path)) {
                throw new SQLException("No existe la base de datos");
            }
            if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
                throw new SQLException("La base de datos no es leible o editable");
            }
        }
    }

    // ── Getter ──────────────────────────────────────────────────────────────
    public String getUrl() { return url; }

    // ── Método estático de fábrica ──────────────────────────────────────────
    /**
     * Punto de entrada principal. Devuelve una Connection lista para usar:
     *   · Activa las claves foráneas (PRAGMA foreign_keys = ON)
     *   · Verifica que existan las 5 tablas del esquema
     * Uso: Connection con = ConexionBD.abrir("jdbc:sqlite:datos/clinica.sqlite3");
     */
    public static Connection abrir(String url) throws SQLException {
        // 1. Valida URL y archivo (lanza si falla)
        new ConexionBD(url);

        // 2. Abre la conexión JDBC real
        Connection connection = DriverManager.getConnection(url);

        // 3. Activa claves foráneas (por defecto SQLite las tiene DESACTIVADAS)
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON;");
        } catch (SQLException e) {
            connection.close();   // Limpia recursos si falla
            throw e;
        }

        // 4. Comprueba que el esquema esté completo (deben existir las 5 tablas)
        String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' "
                   + "AND name IN ('clientes','mascotas','veterinarios','consultas','tratamientos')";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next() || rs.getInt(1) != 5) {
                throw new SQLException("El esquema de la base de datos esta incompleto");
            }
            return connection;  // ✅ Conexión válida lista para usar
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }
}
```

> **TRUCO:** En los DAO siempre se usa `ConexionBD.abrir(url)` dentro de un **try-with-resources**
> para que la conexión se cierre automáticamente aunque haya error.

---

## 5. ⚙️ Funciones CRUD — Patrón de implementación

### 5.1 Interfaz genérica (lo que define cada `IXxxDao`)

```java
List<Entidad>      listar();                  // READ - todos
Optional<Entidad>  buscarPorId(Integer id);   // READ - uno
int                insertar(Entidad e);        // CREATE → devuelve id generado
int                actualizar(Entidad e);      // UPDATE → devuelve filas afectadas
int                eliminar(Integer id);       // DELETE → devuelve filas afectadas
```

### 5.2 Método `mapear(ResultSet rs)` — convierte una fila en objeto

```java
// Extraer columnas por nombre → construir el POJO
private Cliente mapear(ResultSet rs) throws SQLException {
    return new Cliente(
        rs.getInt("id"),
        rs.getString("nombre"),
        rs.getString("email"),
        rs.getString("telefono")
    );
}
// Para tipos numéricos decimales: rs.getBigDecimal("horas")
// Para enteros foráneos:          rs.getInt("id_cliente")
```

---

### 5.3 `listar()` — SELECT todos

```java
public List<Cliente> listar() {
    // SQL: selecciona todas las columnas, ordenado por id
    String sql = "SELECT id, nombre, email, telefono FROM clientes ORDER BY id ASC";
    List<Cliente> lista = new ArrayList<>();

    // try-with-resources: cierra Connection, PreparedStatement y ResultSet automáticamente
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {          // executeQuery() para SELECT

        while (rs.next()) {                           // itera fila a fila
            lista.add(mapear(rs));                    // convierte cada fila en objeto
        }
        return lista;
    } catch (SQLException e) {
        throw new DaoException("Error al listar clientes", e);   // envuelve en RuntimeException
    }
}
```

---

### 5.4 `buscarPorId(Integer id)` — SELECT por clave primaria

```java
public Optional<Cliente> buscarPorId(Integer id) {
    if (id == null) return Optional.empty();          // guarda de null

    String sql = "SELECT id, nombre, email, telefono FROM clientes WHERE id = ?";
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);                             // sustituye el primer '?'

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapear(rs));       // encontrado
            }
            return Optional.empty();                  // no encontrado
        }
    } catch (SQLException e) {
        throw new DaoException("Error al buscar cliente por id", e);
    }
}
```

---

### 5.5 `insertar(Entidad e)` — INSERT + recuperar id generado

```java
public int insertar(Cliente entidad) {
    if (entidad == null) throw new IllegalArgumentException("...");

    String sql = "INSERT INTO clientes (nombre, email, telefono) VALUES (?, ?, ?)";
    // Statement.RETURN_GENERATED_KEYS → pide que SQLite devuelva el id autogenerado
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setString(1, entidad.getNombre());         // parámetro 1
        ps.setString(2, entidad.getEmail());          // parámetro 2
        ps.setString(3, entidad.getTelefono());       // parámetro 3
        ps.executeUpdate();                           // ejecuta INSERT (no devuelve ResultSet)

        try (ResultSet rs = ps.getGeneratedKeys()) {  // obtiene el id generado
            if (rs.next()) {
                int idGenerado = rs.getInt(1);
                entidad.setId(idGenerado);            // actualiza el objeto con su nuevo id
                return idGenerado;
            }
            throw new DaoException("No se obtuvo id generado", null);
        }
    } catch (SQLException e) {
        throw new DaoException("Error al insertar cliente", e);
    }
}
```

> **NOTA:** Si el campo es nullable (p.ej. `raza` en Mascota), usa `ps.setNull(N, java.sql.Types.INTEGER)`
> en vez de `ps.setInt`.

---

### 5.6 `actualizar(Entidad e)` — UPDATE

```java
public int actualizar(Cliente entidad) {
    if (entidad == null || entidad.getId() == null)
        throw new IllegalArgumentException("...");

    String sql = "UPDATE clientes SET nombre = ?, email = ?, telefono = ? WHERE id = ?";
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, entidad.getNombre());
        ps.setString(2, entidad.getEmail());
        ps.setString(3, entidad.getTelefono());
        ps.setInt(4, entidad.getId());                // condición WHERE

        return ps.executeUpdate();                    // devuelve nº filas modificadas (0 ó 1)
    } catch (SQLException e) {
        throw new DaoException("Error al actualizar cliente", e);
    }
}
```

---

### 5.7 `eliminar(Integer id)` — DELETE

```java
public int eliminar(Integer id) {
    Objects.requireNonNull(id, "El id no puede ser nulo");

    String sql = "DELETE FROM clientes WHERE id = ?";
    try (Connection con = ConexionBD.abrir(url);
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);
        return ps.executeUpdate();                    // 1 si borró, 0 si no existía
    } catch (SQLException e) {
        throw new DaoException("Error al eliminar cliente", e);
    }
}
```

> **ADVERTENCIA:** Si hay **claves foráneas** apuntando al registro (`ON DELETE RESTRICT`),
> el DELETE lanzará una `SQLException`. Hay que borrar primero los registros dependientes.

---

### 5.8 Métodos de búsqueda extra (filtros)

```java
// Mascotas de un cliente
String sql = "SELECT ... FROM mascotas WHERE id_cliente = ? ORDER BY id ASC";
ps.setInt(1, idCliente);

// Mascotas por especie
String sql = "SELECT ... FROM mascotas WHERE especie = ? ORDER BY id ASC";
ps.setString(1, especie);

// Consultas por estado fijo
String sql = "SELECT ... FROM consultas WHERE estado = 'PROGRAMADA' ORDER BY id ASC";

// Consultas por veterinario
String sql = "SELECT ... FROM consultas WHERE id_veterinario = ? ORDER BY id ASC";
ps.setInt(1, idVeterinario);
```

---

## 6. 📋 Consultas SQL — Básicas y con subconsultas

### 6.1 Consultas básicas

```sql
-- Todos los registros de una tabla
SELECT * FROM clientes ORDER BY nombre ASC;
SELECT * FROM mascotas ORDER BY id ASC;
SELECT * FROM veterinarios ORDER BY tarifa_hora ASC;

-- Filtrar por valor exacto
SELECT * FROM mascotas WHERE especie = 'Perro';
SELECT * FROM consultas WHERE estado = 'PROGRAMADA';

-- Filtrar por clave foránea
SELECT * FROM mascotas WHERE id_cliente = 1;
SELECT * FROM consultas WHERE id_mascota = 2;
SELECT * FROM consultas WHERE id_veterinario = 3;

-- Buscar por id
SELECT * FROM clientes WHERE id = 1;

-- Ordenar
SELECT * FROM veterinarios ORDER BY tarifa_hora DESC;
SELECT * FROM clientes ORDER BY nombre ASC;

-- Limitar resultados
SELECT * FROM clientes LIMIT 5;

-- Contar registros
SELECT COUNT(*) FROM mascotas;
SELECT COUNT(*) FROM mascotas WHERE especie = 'Gato';

-- Calcular totales
SELECT SUM(cantidad * precio_unitario) AS total FROM tratamientos WHERE id_consulta = 1;
SELECT MAX(tarifa_hora), MIN(tarifa_hora), AVG(tarifa_hora) FROM veterinarios;
```

---

### 6.2 JOINs — Combinar tablas

```sql
-- Mascotas con su propietario (JOIN implícito con WHERE)
SELECT m.id, m.nombre AS mascota, m.especie, c.nombre AS propietario, c.telefono
FROM mascotas m, clientes c
WHERE m.id_cliente = c.id
ORDER BY m.id ASC;

-- Mascotas con su propietario (INNER JOIN explícito — equivalente)
SELECT m.id, m.nombre AS mascota, m.especie, c.nombre AS propietario, c.telefono
FROM mascotas m
INNER JOIN clientes c ON m.id_cliente = c.id
ORDER BY m.id ASC;

-- Consultas con mascota, propietario y veterinario (3 JOINs)
SELECT con.id AS id_consulta, con.fecha, con.estado,
       m.nombre AS mascota, cli.nombre AS propietario, v.nombre AS veterinario
FROM consultas con
INNER JOIN mascotas     m   ON con.id_mascota     = m.id
INNER JOIN clientes     cli ON m.id_cliente        = cli.id
INNER JOIN veterinarios v   ON con.id_veterinario  = v.id
ORDER BY con.id ASC;

-- Clientes con número de mascotas (incluye clientes con 0 mascotas → LEFT JOIN)
SELECT c.id, c.nombre, COUNT(m.id) AS total_mascotas
FROM clientes c
LEFT JOIN mascotas m ON c.id = m.id_cliente
GROUP BY c.id, c.nombre
ORDER BY c.id ASC;

-- Importe total de tratamientos por consulta (incluye consultas sin tratamientos)
SELECT c.id AS id_consulta,
       COALESCE(SUM(t.cantidad * t.precio_unitario), 0) AS total_tratamientos
FROM consultas c
LEFT JOIN tratamientos t ON c.id = t.id_consulta
GROUP BY c.id
ORDER BY c.id ASC;

-- Consultas sin ningún tratamiento (LEFT JOIN + WHERE NULL)
SELECT c.id, c.fecha, c.estado
FROM consultas c
LEFT JOIN tratamientos t ON c.id = t.id_consulta
WHERE t.id IS NULL
ORDER BY c.id ASC;
```

---

### 6.3 Subconsultas

```sql
-- Clientes que tienen alguna mascota (subconsulta con IN)
SELECT * FROM clientes
WHERE id IN (SELECT DISTINCT id_cliente FROM mascotas);

-- Clientes que NO tienen ninguna mascota (subconsulta con NOT IN)
SELECT * FROM clientes
WHERE id NOT IN (SELECT DISTINCT id_cliente FROM mascotas);

-- Mascotas que han tenido al menos una consulta
SELECT * FROM mascotas
WHERE id IN (SELECT DISTINCT id_mascota FROM consultas);

-- Mascotas que NUNCA han tenido consulta
SELECT * FROM mascotas
WHERE id NOT IN (SELECT DISTINCT id_mascota FROM consultas);

-- Veterinarios con tarifa mayor que la tarifa media
SELECT * FROM veterinarios
WHERE tarifa_hora > (SELECT AVG(tarifa_hora) FROM veterinarios);

-- Clientes que tienen más de 1 mascota
SELECT * FROM clientes
WHERE id IN (
    SELECT id_cliente FROM mascotas
    GROUP BY id_cliente
    HAVING COUNT(*) > 1
);

-- Consultas del veterinario más caro
SELECT * FROM consultas
WHERE id_veterinario = (
    SELECT id FROM veterinarios
    ORDER BY tarifa_hora DESC
    LIMIT 1
);

-- Tratamientos de las consultas FINALIZADAS
SELECT * FROM tratamientos
WHERE id_consulta IN (
    SELECT id FROM consultas WHERE estado = 'FINALIZADA'
);

-- Coste total por consulta, solo las que superen 100 euros
SELECT id_consulta, SUM(cantidad * precio_unitario) AS total
FROM tratamientos
GROUP BY id_consulta
HAVING total > 100;

-- Mascotas de clientes cuyo email contiene 'gmail'
SELECT * FROM mascotas
WHERE id_cliente IN (
    SELECT id FROM clientes WHERE email LIKE '%gmail%'
);

-- Veterinarios que nunca han atendido ninguna consulta
SELECT * FROM veterinarios
WHERE id NOT IN (SELECT DISTINCT id_veterinario FROM consultas);

-- Número de consultas por veterinario
SELECT v.nombre, COUNT(c.id) AS total_consultas
FROM veterinarios v
LEFT JOIN consultas c ON v.id = c.id_veterinario
GROUP BY v.id, v.nombre
ORDER BY total_consultas DESC;
```

---

### 6.4 Operadores y funciones útiles

```sql
-- LIKE: búsqueda de texto parcial
WHERE nombre LIKE 'Mar%'     -- empieza por "Mar"
WHERE nombre LIKE '%os'      -- termina en "os"
WHERE nombre LIKE '%an%'     -- contiene "an"

-- BETWEEN: rango de valores
WHERE tarifa_hora BETWEEN 20 AND 50
WHERE fecha BETWEEN '2024-01-01' AND '2024-12-31'

-- IS NULL / IS NOT NULL
WHERE raza IS NULL
WHERE raza IS NOT NULL

-- COALESCE: sustituye NULL por un valor por defecto
SELECT COALESCE(raza, 'Sin raza') AS raza FROM mascotas;

-- GROUP BY + HAVING (filtro sobre agregados)
SELECT especie, COUNT(*) AS total
FROM mascotas
GROUP BY especie
HAVING total > 2;

-- EXISTS (verdadero si la subconsulta devuelve alguna fila)
SELECT * FROM clientes c
WHERE EXISTS (SELECT 1 FROM mascotas m WHERE m.id_cliente = c.id);

-- DISTINCT: eliminar duplicados
SELECT DISTINCT especie FROM mascotas;
SELECT DISTINCT id_veterinario FROM consultas;
```

---

## 7. 🔑 Resumen rápido — Chuleta de examen

| Operación | Java (DAO) | SQL |
|-----------|-----------|-----|
| Listar todos | `ps.executeQuery()` + `while(rs.next())` | `SELECT ... ORDER BY id ASC` |
| Buscar por id | `ps.setInt(1, id)` + `if(rs.next())` | `WHERE id = ?` |
| Insertar | `ps.executeUpdate()` + `getGeneratedKeys()` | `INSERT INTO ... VALUES (?,?,?)` |
| Actualizar | `ps.executeUpdate()` (devuelve nº filas) | `UPDATE ... SET ... WHERE id = ?` |
| Eliminar | `ps.executeUpdate()` (devuelve nº filas) | `DELETE FROM ... WHERE id = ?` |

### Métodos de `PreparedStatement` más usados:
```
ps.setInt(pos, valor)          → para INTEGER / id foráneos
ps.setString(pos, valor)       → para TEXT
ps.setBigDecimal(pos, valor)   → para NUMERIC (tarifa, horas, precio)
ps.setNull(pos, Types.INTEGER) → para FK nullable
ps.executeQuery()              → SELECT (devuelve ResultSet)
ps.executeUpdate()             → INSERT / UPDATE / DELETE (devuelve int)
ps.getGeneratedKeys()          → recuperar id generado tras INSERT
```

### Métodos de `ResultSet` más usados:
```
rs.next()               → avanza a la siguiente fila (true si hay)
rs.getInt("columna")    → lee INTEGER
rs.getString("columna") → lee TEXT
rs.getBigDecimal("col") → lee NUMERIC
```

### `DaoException` — siempre en catch de SQLException:
```java
} catch (SQLException e) {
    throw new DaoException("Mensaje descriptivo", e);
}
```

