# Especificación 4 · Lógica fake del teléfono

**1- Enunciado**

Desarrollar en Java la lógica fake del teléfono: la parte que imita la detección del beacon y el envío del código al servidor sin recurrir a Bluetooth real, de modo que pueda testearse sin móvil ni beacon físicos.

**2- Diseño**

```
INACTIVO --empezarEscaneo()--> ESCANEANDO --llega código--> DETECTADO
                                 ^                           |
                                 +---detenerEscaneo()--------+
                                                             |
                                                   enviarCodigo()
                                                             |
                                          POST ../rest/guardarCodigo.php
                                          cuerpo: {"valor": 1234}
                                                             |
                                         {"resultado":true} o un error

+----------------+   +---------------------+   +----------------+
| LogicaFake     |-->| EscannerBeaconFake  |   | ClienteREST    |
| (estados y     |   | (simula la llegada |   | (interfaz: se  |
|  callbacks)    |   |  de un beacon)      |   | sustituye en   |
|                |   |                     |   | los tests)     |
+----------------+   +---------------------+   +----------------+
```

**3- Aclaraciones**

- El beacon está simulado y no se usa Bluetooth, así que el código corre en cualquier ordenador.
- ClienteREST es una interfaz: la implementación auténtica realiza la petición HTTP (puede apoyarse en el PeticionarioREST del curso) y en los tests se emplea un doble que entrega respuestas ya preparadas (respuesta válida, rechazo del servidor, fallo de red). Así se reproducen los errores sin red.
- Para la UI quedan tres callbacks: cambio de estado, detección de un código y finalización del envío.

**4- Ficheros**

- `logicaFakeTelefono/EstadoLogica.java` — estados: INACTIVO, ESCANEANDO, DETECTADO, ENVIADO.
- `logicaFakeTelefono/EscannerBeacon.java` — interfaz del escáner.
- `logicaFakeTelefono/EscannerBeaconFake.java` — la simulación.
- `logicaFakeTelefono/ClienteREST.java` — interfaz del envío con su callback (código HTTP + cuerpo).
- `logicaFakeTelefono/ClienteRESTHttp.java` — implementación real.
- `logicaFakeTelefono/LogicaFake.java` — la máquina de estados.
- `tests/TestLogicaFakeTelefono.java` — tests con main() y asserts propios (sin JUnit): `java tests/TestLogicaFakeTelefono.java`.

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
// -> empezarEscaneo() -> bool
//
//---------------------------------------------------------
//
// -> obtenerCodigoDetectado() -> Entero | error:Texto
//
//---------------------------------------------------------
//
// -> enviarCodigo() -> bool | error:Texto
//
//---------------------------------------------------------
```

Comentarios breves que dejen claro el propósito.

**6- Tests automáticos**

- Detección simulada del beacon (se arranca el escaneo, se simula la llegada del beacon y se notifica el código).
- Obtención del código (antes de detectar nada ofrece error; después devuelve el código).
- Envío simulado (el doble del servidor responde bien: se pasa a ENVIADO y el cuerpo transmitido era el correcto).
- Errores simulados (enviar sin código detectado; el servidor rechaza el código; fallo de red; detener estando ya inactivo).

La salida sigue el patrón de todas las especificaciones: `OK:` / `FALLO:`, recuento `X/Y tests correctos` y exit con error si alguno falla.
