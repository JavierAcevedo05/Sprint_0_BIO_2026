# Especificación 2 · Lógica de negocio del backend (la real)

**1- Enunciado**

Implementar en PHP la lógica de negocio auténtica del servidor: la capa encargada de decidir si un código es válido, almacenarlo en la BD y recuperarlo.

**2- Diseño**

```
guardarCodigo( valor:Entero ) -> bool
------------------------------------
¿es un entero entre 0 y 65535?  --NO--> devuelve false
|
SÍ -> lo inserta en la tabla con fecha = ahora  --> devuelve true

consultarUltimoCodigo() -> (valor:Entero, fecha:Fecha) | error:Texto
-------------------------------------------------------------------
SELECT ... ORDER BY id DESC LIMIT 1
|
¿hay fila?  --NO--> error: "no hay códigos guardados"
SÍ -> devuelve el valor y la fecha
```

**3- Aclaraciones**

- Regla de negocio: un código solo vale si es un entero comprendido entre 0 y 65535; cualquier otra entrada se descarta devolviendo false.
- La fecha la fija el servidor en el instante del guardado.
- Cuando la BD está vacía, la consulta devuelve un mensaje de error.

**4- Ficheros**

- `logica/config.php` — la función que entrega la conexión a la BD.
- `logica/guardarCodigo.php` — guardarCodigo().
- `logica/consultarUltimoCodigo.php` — consultarUltimoCodigo().
- `tests/test_logica.php` — tests en PHP, se ejecutan con `php tests/test_logica.php`.

**5- Comentarios**

Doble pared entre bloques:

```
//---------------------------------------------------------
//---------------------------------------------------------
```

Cabecera de cada función:

```
//---------------------------------------------------------
//
// valor:Entero -> guardarCodigo() -> bool
//
//---------------------------------------------------------
//
// -> consultarUltimoCodigo() -> (valor:Entero, fecha:Fecha) | error:Texto
//
//---------------------------------------------------------
```

Comentarios breves y aclaradores.

**6- Tests automáticos**

- Código válido recibido (1234 devuelve true).
- Código inválido rechazado (-1, 65536, texto, vacío -> false y ninguna fila insertada).
- Guardado correcto (tras guardar, el código aparece en la tabla).
- Consulta correcta (guardo 111 y después 222, y la consulta devuelve 222).
- Fecha de recepción registrada (bien escrita y cercana a la hora actual).

Misma salida que el resto: `OK:` / `FALLO:`, recuento final y exit con error si algo falla. Los tests limpian sus datos al arrancar para que se puedan repetir.
