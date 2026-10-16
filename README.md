# Sprint 0 BIO — Medición de CO₂ con beacon BLE

**Fecha:** 2026-10-16
**Autor:** Javier Acevedo

## Descripción

Sistema que **capta una medición de CO₂ emitida por un beacon Bluetooth**, la
**envía a un servidor**, la **guarda en una base de datos** y la **muestra en una
página web**.

Flujo de datos:

```
BEACON  ->  APP MÓVIL  ->  SERVIDOR  ->  BASE DE DATOS  ->  WEB
(emite)     (escucha+envía)  (valida)     (guarda)        (muestra)
```

- **Beacon (Arduino/nRF52840):** emite por BLE una trama iBeacon con UUID
  `EPSG-GTI-PROY-3A` y el valor de CO₂ en el campo `minor` (hoy 67, de reserva).
- **App Android (Java):** escanea beacons (sin conectar), reconoce su UUID,
  extrae el `minor` y lo envía por HTTP al servidor.
- **Servidor (PHP + MySQL):** valida el valor (entero 0..65535) y lo guarda.
- **Web (HTML/JS):** consulta al servidor y muestra el último valor guardado.

## Arquitectura por capas

- **Backend:** `communication/` -> `business_logic/` -> `database/`
  (la lógica de negocio no depende del transporte).
- **Web:** `gui/` -> `frontend_business_logic/` -> (HTTP) -> `communication/`
  (la GUI nunca hace HTTP directamente).
- **Móvil:** `MainActivity` -> `LogicaFake` (interfaz `EscannerBeacon` +
  interfaz `ClienteREST`). Las implementaciones reales
  (`EscannerBeaconBTLE`, `ClienteRESTHttp`) se inyectan por constructor;
  en los tests se inyectan dobles.

## Estructura del repositorio

```
Proyecto/
├── arduino/                     Firmware del beacon (nRF52840)
├── AndroidApp/                  App Android (escáner + envío)
├── logicaFakeTelefono/          Lógica del teléfono portable (para pruebas en PC)
├── src/
│   ├── communication/           Endpoints REST (PHP)
│   ├── business_logic/          Reglas de negocio (PHP)
│   ├── database/                Esquema SQL (MySQL)
│   ├── frontend_business_logic/ Proxy de lógica del cliente web (JS)
│   └── gui/                     Interfaz web (HTML/JS)
├── doc/                         Diseños de los componentes
├── tests/                       Pruebas automatizadas
├── Tarea2/                      Enunciados de las tareas del sprint
├── author.md
└── README.md
```

## Requisitos

- **XAMPP** (PHP y MySQL).
- **Node.js** (para los tests de la lógica web).
- **JDK de Android Studio** (para los tests de la lógica del teléfono).
- **Arduino IDE / nRF52840** (solo para grabar el beacon).

## Puesta en marcha del servidor

Desde la carpeta `Proyecto/`:

```bash
C:\xampp\php\php.exe -S 0.0.0.0:8080 -t .
```

Después, abrir en el navegador:

```
http://localhost:8080/src/gui/Aplicacion.html
```

> El servidor debe quedar con la ventana abierta mientras se usa la web.

## Ejecutar los tests

Todos los comandos se lanzan **desde la carpeta `Proyecto/`**.

### 1. Base de datos (7 tests)

Requiere MySQL (XAMPP) encendido. Crea la base a partir de `crear_bd.sql`.

```bash
C:\xampp\php\php.exe tests\test_bd.php
```

### 2. Lógica de negocio backend (9 tests)

Requiere MySQL encendido y la base `bd_codigos` creada.

```bash
C:\xampp\php\php.exe tests\test_logica.php
```

### 3. Servidor REST (5 tests)

Requiere el servidor arrancado en otra ventana.

```bash
C:\xampp\php\php.exe tests\test_rest.php
```

### 4. Lógica del cliente web (2 tests)

No necesita red ni servidor (usa un doble de `XMLHttpRequest`).

```bash
node tests\test_logica_fake_web.js
```

### 5. Lógica del teléfono (16 tests)

Usa dobles del escáner, del cliente REST y de la UI.

```bash
"C:\Program Files\Android\Android Studio\jbr\bin\javac.exe" -d out logicaFakeTelefono\*.java tests\TestLogicaFakeTelefono.java
"C:\Program Files\Android\Android Studio\jbr\bin\java.exe" -cp out TestLogicaFakeTelefono
```

### Resultados esperados

| Test | Resultado |
|------|-----------|
| `test_bd.php` | 7/7 |
| `test_logica.php` | 9/9 |
| `test_rest.php` | 5/5 |
| `test_logica_fake_web.js` | 2/2 |
| `TestLogicaFakeTelefono` | 16/16 |

> **Aviso:** `test_bd.php`, `test_logica.php` y `test_rest.php` vacían la tabla
> `CODIGO` (`TRUNCATE`) al empezar y/o terminar, así que borran los códigos
> guardados. No los ejecutes justo antes de una demostración con datos.
