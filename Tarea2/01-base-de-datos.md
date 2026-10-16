# Especificación 1 · Base de datos

**1- Enunciado**

Hay que montar en MySQL el almacén donde el servidor dejará registrados los códigos que llegan del beacon, junto a una batería de tests automáticos que comprueben que todo funciona como debe.

**2- Diseño**

```
BASE DE DATOS: bd_codigos
TABLA: CODIGO
-------------
id     (int, clave primaria, autoincrementa)
valor  (int, obligatorio, entre 0 y 65535)
fecha  (fecha y hora, obligatoria)
```

El campo `valor` guarda el código que trae el beacon. Al ser un entero, esa condición se impone mediante una restricción CHECK. La fecha recoge el momento en que el servidor recibe dicho código.

Cada envío desde el móvil deja una fila nueva: nada se borra ni se actualiza. La web siempre solicita el último código guardado, o sea la fila de id más alto.

**3- Ficheros**

- `bd/crear_bd.sql` — crea la BD y la tabla, con todas sus restricciones.
- `tests/test_bd.php` — los tests, escritos en PHP. Se lanzan con `php tests/test_bd.php`.

**4- Comentarios**

Las distintas partes del código se separan con doble pared:

```
//---------------------------------------------------------
//---------------------------------------------------------
```

Y cada bloque abre con su cabecera:

```
//---------------------------------------------------------
//
// id:Entero, valor:Entero, fecha:Fecha -> tabla CODIGO() -> bool
//
//---------------------------------------------------------
```

Comentarios cortos que expliquen la utilidad de cada fragmento.

**5- Tests automáticos**

- Creación de la tabla.
- Inserción de un código.
- Consulta de un código verificando que devuelve el último guardado.
- Restricciones: que fallen los valores fuera de rango o los campos obligatorios vacíos.

Cada test escribe `OK: nombre` o `FALLO: nombre - motivo`, y al final aparece el recuento `X/Y tests correctos`. Si alguno falla, el script termina con código de error.
