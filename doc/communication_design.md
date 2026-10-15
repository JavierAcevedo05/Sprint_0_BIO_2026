# Diseño del Componente — COMUNICACIÓN / SERVIDOR REST (`communication_design.md`)

## 1. Component Design (Diseño del Componente)

Componente de comunicación del backend. Expone los puntos de entrada externos por
HTTP (REST) que usan el móvil (para enviar el código) y la web (para consultarlo).
Por debajo **invoca la lógica de negocio**; aquí no se implementa ninguna regla de
negocio, sólo se traduce de HTTP a llamadas de dominio y de vuelta a JSON.

```
MÓVIL                                    WEB
  |                                       |
  | POST ../src/communication/            | GET ../src/communication/
  |      guardarCodigo.php                |     consultarCodigo.php
  | cuerpo: {"valor": 1234}               | (sin parámetros)
  v                                       v
--------------------------------------------------------
  SERVIDOR REST
  1. recoge los parámetros
  2. llama a la lógica de negocio    (src/business_logic)
  3. devuelve el resultado en JSON
--------------------------------------------------------
```

Endpoints:

```
POST /src/communication/guardarCodigo.php
  entrada : cuerpo JSON { valor: Z }
  salida  : { resultado: B }                        (éxito)
            { resultado: B, error: Text }           (fallo)

GET  /src/communication/consultarCodigo.php
  entrada : (ninguna)
  salida  : { valor: N, fecha: Text, error: N }     (éxito, error = 0)
            { error: Text }                         (no hay códigos)
```

Mapeo con la lógica de negocio:

```
valor: Z --> guardarCodigo() --> B                  (POST)
         --> consultarUltimoCodigo() --> Codigo | error: Text   (GET)
```

## 2. Design Clarifications (Aclaraciones del Diseño)

- Se usa **POST** para enviar un código y **GET** para consultar.
- El nombre de cada fichero REST coincide con el de la operación de la lógica.
- Si el código no es válido, se le pasa igualmente a la lógica y es ésta la que
  devuelve `false`; la comunicación sólo informa del resultado.
- La comunicación es la **única** capa que conoce HTTP y JSON.

## 3. General Rules (Reglas Generales)

- **Lenguaje de programación:** PHP.
- **Encabezados:** cada endpoint lleva un bloque de comentario delimitado por líneas
  discontinuas (`--------------------`) que indica el método HTTP, la ruta, el cuerpo de
  entrada y la estructura de la respuesta.
- **Legibilidad del código:** el código debe ser claro y autoexplicativo.
- **Pruebas automatizadas:** se generan pruebas de integración con peticiones HTTP reales:
  guardar válido, guardar fuera de rango, cuerpo mal formado, consultar y consultar con BD
  vacía (`src/communication` → probadas por `tests/test_rest.php`).
- **Dependencia:** esta capa puede depender de `src/business_logic`; nunca al revés.
