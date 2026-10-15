# Diseño del Componente — INTERFAZ DE USUARIO WEB (`gui_design.md`)

> Nota: la verificación del diseño de la GUI queda **fuera del alcance** de la
> revisión del agente. Este documento sólo describe textualmente la pantalla, tal
> como se pide.

## 1. Diseño del Componente

Una única página (`src/gui/Aplicacion.html`) con fondo blanco, sin menú ni cabecera,
con un texto centrado horizontal y verticalmente que muestra el código consultado.

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

Responsabilidades de la GUI:

```
-> pintar( texto: Text ) --> B        (escribe el texto en pantalla)
-> cargarAplicacion() --> B           (consulta el último código y lo pinta)
```

La GUI **no** hace HTTP: al cargar, llama a `consultarUltimoCodigo()` de
`src/frontend_business_logic` y pinta el resultado.

## 2. Aclaraciones del Diseño

- El CSS va en un `<style>` dentro del propio HTML; charset UTF-8 y título
  "Beacon - Consulta de código".
- Un único texto en pantalla; el centrado se consigue con `flex` en el `body`.
- La única dependencia de la GUI es la lógica de negocio del cliente
  (`frontend_business_logic`).

## 3. Reglas Generales

- **Lenguaje de programación:** HTML + CSS + JavaScript (navegador).
- **Encabezados:** las funciones JavaScript de la GUI llevan un bloque de comentario
  delimitado por líneas discontinuas (`--------------------`).
- **Legibilidad del código:** claro y autoexplicativo.
- **Pruebas automatizadas:** se prueban el estado inicial, el pintado del código,
  la ausencia de código y el centrado (`src/gui` → `tests/test_ux.html`).
