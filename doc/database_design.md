# Diseño del Componente — BASE DE DATOS (`database_design.md`)

## 1. Diseño del Componente

Base de datos relacional MySQL llamada `bd_codigos`. Guarda los códigos que el
móvil envía al servidor tras detectar el beacon. Cada envío es una fila nueva:
nunca se borra ni se actualiza. La web siempre pide el último código guardado
(la fila de mayor `id`).

```
====================================================================================
TABLE: CODIGO
DESCRIPTION: Almacena los códigos recibidos del beacon, con la fecha en que el
servidor los recibió. Una fila por cada envío.
COLUMNS:

+ id | INT | NOT NULL | Auto-Increment
+ valor | INT | NOT NULL
+ fecha | DATETIME | NOT NULL

PRIMARY KEY: id

FOREIGN KEYS:

+ (ninguna)

CONSTRAINTS:

+ CHECK (valor BETWEEN 0 AND 65535)
====================================================================================
```

## 2. Aclaraciones del Diseño

- `id` es la clave primaria y autoincrementa; identifica de forma unívoca cada envío y
  define cuál es el "último código".
- `valor` es el código que traía el beacon. Un código válido es un entero entre `0` y
  `65535` (2 bytes sin signo, como el `minor` de un iBeacon); de ahí la restricción `CHECK`.
- `fecha` la pone el servidor con `NOW()` en el momento de guardar (no viene del móvil).
- No hay claves ajenas: es una única tabla.
- El motor es InnoDB y el juego de caracteres `utf8mb4`.

## 3. Reglas Generales

- **Lenguaje de programación:** SQL (MySQL 8 / MariaDB de XAMPP).
- **Encabezados:** el script de creación de la tabla lleva un bloque de comentario
  delimitado por líneas discontinuas (`--------------------`) que describe la tabla y
  cada columna.
- **Legibilidad del código:** el SQL debe ser claro y autoexplicativo, con el mínimo
  de comentarios adicionales.
- **Pruebas automatizadas:** se generan pruebas de integración para la creación de la
  tabla, la inserción, la consulta del último código y las restricciones
  (`src/database` → probadas por `tests/test_bd.php`).
