# Especificación 3 · Servidor REST del backend

**1- Enunciado**

Crear las rutas REST del servidor PHP, las que consumen el móvil (para subir el código) y la web (para leerlo). Por debajo invocan a la lógica de negocio; aquí no se implementa ninguna regla, únicamente traducir entre HTTP y PHP.

**2- Diseño**

```
MÓVIL                                  WEB
 |                                      |
 | POST ../rest/guardarCodigo.php       | GET ../rest/consultarCodigo.php
 | cuerpo: {"valor": 1234}              | (sin parámetros)
 v                                      v
--------------------------------------------------------
  SERVIDOR REST                                         
  1. recoge los parámetros                              
  2. llama a la lógica de negocio                       
  3. devuelve el resultado en JSON                      
--------------------------------------------------------

Respuestas:
 guardar:   {"resultado": true}   o   {"resultado": false, "error": "..."}
 consultar: {"valor": 1234, "fecha": "...", "error": 0}
            {"error": "no hay códigos guardados"}
```

**3- Aclaraciones**

- GET para la consulta y POST para el envío.
- Los nombres de los ficheros REST siguen los de las funciones de la lógica.
- Si el código no cumple las condiciones, se le pasa a la lógica y ésta responde false.

**4- Ficheros**

- `rest/guardarCodigo.php`
- `rest/consultarCodigo.php`
- `tests/test_rest.php` — lanza peticiones HTTP de verdad, así que hay que tener el servidor en marcha (`php -S localhost:8080 -t .`) en una terminal y lanzar los tests en otra.

**5- Comentarios**

Doble pared y cabecera:

```
//---------------------------------------------------------
//
// POST ../rest/guardarCodigo.php
// cuerpo JSON: {"valor":Entero}
//
// valor:Entero -> guardarCodigo() -> bool | error:Texto
//
// @return
//  resultado:bool
//  error:Texto   (solo cuando resultado = false)
//
//---------------------------------------------------------
```

Comentarios breves y explicativos.

**6- Tests automáticos**

- Enviar por POST un valor válido -> resultado:true.
- Enviar por POST un valor fuera de rango -> resultado:false.
- Enviar un cuerpo mal construido.
- Consultar -> devuelve valor, fecha y error.
- Consultar con la BD vacía -> devuelve el mensaje de error.

Los tests vacían la tabla al inicio. El formato de salida es el mismo que en
todas las especificaciones: `OK:` / `FALLO:`, recuento y exit con error si
alguno falla.
