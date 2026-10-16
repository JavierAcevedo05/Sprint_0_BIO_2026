# Especificación 5 · Lógica fake del navegador

**1- Enunciado**

Crear en JavaScript la lógica fake del navegador: la función que la página web invoca para pedirle al servidor el último código recibido.

**2- Diseño**

```
consultarUltimoCodigo( cb )
      |
      |  XMLHttpRequest  GET  ../rest/consultarCodigo.php
      v
Lo que puede devolver el servidor:
   {"valor":1234,"fecha":"...","error":0}   -> cb(null, res)
   {"error":"no hay códigos guardados"}     -> cb(err, null)
   fallo HTTP o JSON que no es JSON         -> cb(err, null)
```

**3- Aclaraciones**

- Esta función no incluye lógica de negocio: solo lanza la petición y traduce la respuesta.
- La URL es relativa al HTML (`../rest/...`).
- Detalle clave para los tests: el XMLHttpRequest debe poder sustituirse (por ejemplo con una función crearXHR() que los tests reemplazan).

**4- Ficheros**

- `ux/logicaFake/consultarUltimoCodigo.js`
- `tests/test_logica_fake_web.js`

**5- Comentarios**

Doble pared y cabecera:

```
// ---------------------------------------------------
//
// versión fake de una función de la lógica
//
// -> consultarUltimoCodigo() -> (valor:Entero, fecha:Fecha) | error:Texto
//
// (valor:Entero, fecha:Fecha) | error:Texto : devuelto via callback( err, res )
//
// ---------------------------------------------------
```

Comentarios breves y aclarativos.

**6- Tests automáticos**

- Obtención de código (el doble responde con un valor).
- Ausencia de código (el doble responde con un error).

La salida es la misma que en el resto de especificaciones: `OK:` / `FALLO:`, recuento `X/Y tests correctos` y exit con error si alguno falla.
