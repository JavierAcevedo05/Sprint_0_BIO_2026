# Diseño del Componente — LÓGICA DE NEGOCIO DEL BACKEND (`business_logic_design.md`)

## 1. Component Design (Diseño del Componente)

La lógica de negocio es la capa que decide si un código es válido, lo guarda en la
base de datos y lo consulta. Está **completamente desacoplada** de la capa de
comunicación y de la red: sólo trabaja con tipos de dominio y con el esquema de la
base de datos.

Tipos usados (según `context/Software_Engineering_Spec.md`):

```
N        = número natural
Z        = número entero
B        = booleano
Text     = texto
Codigo   = ( valor: N, fecha: Text )        // agregado
```

Firma lógica de las operaciones:

```
valor: Z  --> guardarCodigo() --> B

          --> consultarUltimoCodigo() --> Codigo | error: Text
```

Comportamiento:

```
guardarCodigo( valor: Z ) -> B
------------------------------------
¿valor es entero entre 0 y 65535?  --NO--> devuelve false
         |
         SÍ --> inserta fila en CODIGO con fecha = ahora --> devuelve true

consultarUltimoCodigo() -> Codigo | error: Text
-------------------------------------------------------------------
SELECT valor, fecha FROM CODIGO ORDER BY id DESC LIMIT 1
         |
¿hay fila?  --NO--> error: "no hay códigos guardados"
         SÍ --> devuelve ( valor, fecha )
```

### Relación con la base de datos (según `database_design.md`)

La lógica de negocio es consciente del esquema de datos y opera sobre la única tabla
definida en `database_design.md`:

- Tabla **`CODIGO`**, columnas `id` (INT, PK, autoincremento), `valor` (INT), `fecha` (DATETIME).
- `guardarCodigo()` **inserta** en `CODIGO` (`valor`, `fecha`).
- `consultarUltimoCodigo()` **lee** de `CODIGO` la fila con mayor `id` y devuelve `valor` y `fecha`.

La capa obtiene la conexión a la base de datos a través de una función de acceso
(`getConexion()`), que devuelve un recurso de conexión al esquema de `database_design.md`.

## 2. Design Clarifications (Aclaraciones del Diseño)

- Regla de negocio: un código válido es un entero entre `0` y `65535`. Todo lo demás
  se rechaza devolviendo `false` y **sin insertar ninguna fila**.
- La fecha la pone el servidor en el momento de guardar.
- Si la base de datos está vacía, `consultarUltimoCodigo()` devuelve un error con texto.
- Las funciones devuelven tipos de dominio; la forma concreta de devolver el resultado
  (objeto agregado, estructura o tupla) la decide la implementación, pero su **firma
  lógica** es la indicada arriba.

### Resumen de la API lógica

| Operación | Entrada | Salida |
| --- | --- | --- |
| `guardarCodigo` | `valor: Z` | `B` |
| `consultarUltimoCodigo` | — | `Codigo` \| `error: Text` |

## 3. General Rules (Reglas Generales)

- **Lenguaje de programación:** PHP.
- **Encabezados:** cada función incluye su diseño lógico dentro de un bloque de
  comentario delimitado por líneas discontinuas (`--------------------`).
- **Legibilidad del código:** el código debe ser claro y autoexplicativo, con el mínimo
  de comentarios adicionales.
- **Pruebas automatizadas:** se generan pruebas para los métodos críticos: código válido,
  rechazo de inválidos, almacenamiento correcto, consulta correcta y registro de la fecha
  (`src/business_logic` → probadas por `tests/test_logica.php`).
- **Independencia de comunicación:** esta capa no puede depender de la capa de
  comunicación ni de la red; sólo de la base de datos (`database_design.md`).
