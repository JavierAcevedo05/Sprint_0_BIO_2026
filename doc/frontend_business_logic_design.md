# Diseño del Componente — LÓGICA DE NEGOCIO DEL CLIENTE / FRONTEND (`frontend_business_logic_design.md`)

## 1. Diseño del Componente

Lógica de negocio del cliente para la web. Presenta a la GUI **la misma interfaz
limpia de dominio** que la lógica de negocio del backend, pero su implementación es
un **proxy/fake** que encapsula la llamada de comunicación (HTTP) al backend. La GUI
nunca hace HTTP: sólo llama a este componente.

Operaciones expuestas (subconjunto de la lógica del backend que usa la web):

```
-> consultarUltimoCodigo() --> Codigo | error: Text

Codigo = ( valor: N, fecha: Text )
```

Identidad de firmas con el backend (`business_logic_design.md`): el nombre, los tipos
de parámetros y el tipo de retorno son idénticos. La entrega del resultado se hace
mediante un *callback* `cb( err: Text | null, res: Codigo | null )`, que es un
mecanismo de lenguaje (se omite en el diseño lógico, igual que en el ejemplo de la
especificación).

Comportamiento del proxy (versión fake):

```
-> consultarUltimoCodigo() -> (valor: N, fecha: Text) | error: Text
---------------------------------------------------------
GET ../communication/consultarCodigo.php
   |
   ¿respuesta correcta?  --NO--> cb( error, null )
   SÍ                        --> cb( null, resultado )
```

## 2. Aclaraciones del Diseño

- El "fake" encapsula la comunicación: la GUI ve sólo `consultarUltimoCodigo()`.
- El `XMLHttpRequest` se obtiene a través de `crearXHR()`, para que los tests puedan
  sustituirlo por un doble y no depender de la red.
- La URL es relativa al documento HTML (`../communication/consultarCodigo.php`),
  manteniendo el desacoplamiento de la ubicación del backend.
- Se admiten errores del servidor (código no válido) y fallos de red/HTTP.

## 3. Reglas Generales

- **Lenguaje de programación:** JavaScript (navegador).
- **Encabezados:** cada función lleva un bloque de comentario delimitado por líneas
  discontinuas (`--------------------`) con su diseño lógico.
- **Legibilidad del código:** claro y autoexplicativo.
- **Pruebas automatizadas:** obtención de código y ausencia de código, con un doble del
  `XMLHttpRequest` (`src/frontend_business_logic` → probadas por `tests/test_logica_fake_web.js`).
- **Desacoplamiento:** la GUI (`src/gui`) interactúa exclusivamente con este componente;
  nunca contiene lógica HTTP/fetch/WebSocket.
