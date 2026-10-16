# Especificación 6 · UX en el navegador

**1- Enunciado**

Montar la interfaz de la página web: un texto en medio de la pantalla que muestra el código consultado al servidor.

**2- Diseño**

```
+------------------------------------------+
|  (fondo blanco, sin menú ni cabecera)    |
|                                          |
|            Código: 1234                  |  <- texto centrado
|                                          |     (horizontal y
+------------------------------------------+       vertical)

Estados del texto:
  al cargar:           "Consultando..."
  si llega el código:  "Código: 1234"
  si no hay nada:      "No hay código"
```

**3- Aclaraciones**

- Un único texto en pantalla.
- El CSS vive dentro del propio HTML (un <style> en el head).
- Charset UTF-8 y título "Beacon - Consulta de código".

**4- Ficheros**

- `ux/Aplicacion.html` — la página completa (HTML + CSS + un JS mínimo con la función que pinta el texto y la llamada a la fake al cargar).
- `tests/test_ux.html` — tests: se abren con doble clic en el navegador, sin servidor. Para no depender de la red, sustituyen la función fake por un doble que devuelve respuestas preparadas (código, ausencia de código, error).

**5- Comentarios**

Paredes HTML:

```
<!-- ---------------------------------------------------- -->
<!-- ---------------------------------------------------- -->
```

Cabecera del JS:

```
// ---------------------------------------------------
//
// -> pintar( texto:Texto ) -> bool
//
// ---------------------------------------------------
//
// -> cargarAplicacion() -> bool
//   (consulta el último código y lo pinta en pantalla)
//
// ---------------------------------------------------
```

Comentarios breves de finalidad.

**6- Tests automáticos**

- Pintado del código (la fake devuelve el código y en pantalla aparece "Código: 1234").
- Ausencia de código (la fake devuelve error y aparece "No hay código").
- Estado inicial (al cargar, antes de la respuesta, se ve "Consultando...").
- Texto centrado en horizontal y en vertical.

Cada test imprime `OK:` o `FALLO:` y al final el recuento `X/Y tests correctos`.
