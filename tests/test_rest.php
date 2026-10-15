<?php

// ----------------------------------------------------------
//
// Tests del servidor REST
// (src/communication/guardarCodigo.php y src/communication/consultarCodigo.php)
//
// Hacen peticiones HTTP reales, así que requieren tener el
// servidor arrancado. Por ejemplo, desde la carpeta del proyecto:
//
//   php -S localhost:8080 -t .
//
// y en otra terminal:
//
//   php tests/test_rest.php
//
// ----------------------------------------------------------

require_once __DIR__ . "/../src/business_logic/config.php";

$BASE_URL = "http://localhost:8080";

$testsCorrectos = 0;
$testsTotal = 0;

// ----------------------------------------------------------
//
// nombre:Texto, condicion:VoF, motivo:Texto -> test() -> nada
//
// Imprime OK o FALLO y lleva el recuento final.
//
// ----------------------------------------------------------

function test($nombre, $condicion, $motivo = "") {
    global $testsCorrectos, $testsTotal;

    $testsTotal++;

    if ($condicion) {
        $testsCorrectos++;
        echo "OK: $nombre\n";
    } else {
        echo "FALLO: $nombre" . ($motivo !== "" ? " - $motivo" : "") . "\n";
    }
}

// ----------------------------------------------------------
//
// metodo:Texto, ruta:Texto, cuerpo:Texto -> peticion() -> objeto|nada
//
// Hace una petición HTTP y devuelve el JSON recibido como objeto.
//
// ----------------------------------------------------------

function peticion($metodo, $ruta, $cuerpo = null) {
    global $BASE_URL;

    $opciones = array(
        "http" => array(
            "method"        => $metodo,
            "header"        => "Content-Type: application/json; charset=utf-8\r\n",
            "ignore_errors" => true,
            "timeout"       => 10
        )
    );

    if ($cuerpo !== null) {
        $opciones["http"]["content"] = $cuerpo;
    }

    $contexto = stream_context_create($opciones);
    $respuesta = @file_get_contents($BASE_URL . $ruta, false, $contexto);

    if ($respuesta === false) {
        return null;
    }

    return json_decode($respuesta, true);
}

// ----------------------------------------------------------
//
// -> limpiarTabla() -> nada
//
// Borra todas las filas para poder repetir los tests.
//
// ----------------------------------------------------------

function limpiarTabla() {
    $conexion = getConexion();
    $conexion->query("TRUNCATE TABLE CODIGO");
    $conexion->close();
}

// comprobamos que el servidor responde antes de empezar
if (peticion("GET", "/src/communication/consultarCodigo.php") === null) {
    echo "El servidor no responde en $BASE_URL.\n";
    echo "Arranca el servidor con: php -S localhost:8080 -t .\n";
    exit(1);
}

// limpiamos la tabla al empezar
limpiarTabla();

// ----------------------------------------------------------
// 1. guardar por POST con un valor válido
// ----------------------------------------------------------
$resultado = peticion("POST", "/src/communication/guardarCodigo.php", json_encode(array("valor" => 1234)));
test("guardar por POST con valor válido",
    is_array($resultado) && isset($resultado["resultado"]) && $resultado["resultado"] === true,
    "no devolvió resultado:true");

// ----------------------------------------------------------
// 2. guardar por POST con un valor fuera de rango
// ----------------------------------------------------------
$resultado = peticion("POST", "/src/communication/guardarCodigo.php", json_encode(array("valor" => 70000)));
test("guardar por POST con valor fuera de rango",
    is_array($resultado) && isset($resultado["resultado"]) && $resultado["resultado"] === false,
    "no devolvió resultado:false");

// ----------------------------------------------------------
// 3. guardar con el cuerpo mal formado
// ----------------------------------------------------------
$resultado = peticion("POST", "/src/communication/guardarCodigo.php", "{ esto no es json ");
test("guardar con el cuerpo mal formado",
    is_array($resultado) && isset($resultado["resultado"]) && $resultado["resultado"] === false,
    "no devolvió resultado:false");

// ----------------------------------------------------------
// 4. consultar -> devuelve valor, fecha y error
// ----------------------------------------------------------
$resultado = peticion("GET", "/src/communication/consultarCodigo.php");
test("consultar devuelve valor, fecha y error",
    is_array($resultado)
        && isset($resultado["valor"]) && $resultado["valor"] == 1234
        && isset($resultado["fecha"])
        && isset($resultado["error"]) && $resultado["error"] === 0,
    "no devolvió el código, la fecha o el error esperados");

// ----------------------------------------------------------
// 5. consultar con la BD vacía -> error de texto
// ----------------------------------------------------------
limpiarTabla();
$resultado = peticion("GET", "/src/communication/consultarCodigo.php");
test("consultar con la BD vacía devuelve error",
    is_array($resultado) && isset($resultado["error"]) && $resultado["error"] == "no hay códigos guardados",
    "no devolvió el error de texto");

// ----------------------------------------------------------
// recuento final
// ----------------------------------------------------------
echo "\n$testsCorrectos/$testsTotal tests correctos\n";

if ($testsCorrectos != $testsTotal) {
    exit(1);
}
