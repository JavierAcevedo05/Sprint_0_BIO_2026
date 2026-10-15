<?php

// ----------------------------------------------------------
//
// Tests de la lógica de negocio
// (src/business_logic/guardarCodigo.php y src/business_logic/consultarUltimoCodigo.php)
// Ejecutar con: php tests/test_logica.php
//
// ----------------------------------------------------------

require_once __DIR__ . "/../src/business_logic/guardarCodigo.php";
require_once __DIR__ . "/../src/business_logic/consultarUltimoCodigo.php";

mysqli_report(MYSQLI_REPORT_ERROR | MYSQLI_REPORT_STRICT);

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
// -> contarFilas() -> Entero
//
// Devuelve cuántas filas hay en la tabla CODIGO.
//
// ----------------------------------------------------------

function contarFilas() {
    $conexion = getConexion();
    $resultado = $conexion->query("SELECT COUNT(*) AS n FROM CODIGO");
    $fila = $resultado->fetch_assoc();
    $conexion->close();
    return (int) $fila["n"];
}

// limpiamos sus datos al empezar para poder repetir los tests
$conexion = getConexion();
$conexion->query("TRUNCATE TABLE CODIGO");
$conexion->close();

// ----------------------------------------------------------
// 1. recepción de un código válido
// ----------------------------------------------------------
test("recepción de un código válido", guardarCodigo(1234) === true, "no devolvió true");

// ----------------------------------------------------------
// 2. rechazo de códigos inválidos (sin insertar ninguna fila)
// ----------------------------------------------------------
$filasAntes = contarFilas();

test("rechazo del valor -1",    guardarCodigo(-1) === false, "aceptó -1");
test("rechazo del valor 65536", guardarCodigo(65536) === false, "aceptó 65536");
test("rechazo del valor texto", guardarCodigo("texto") === false, "aceptó un texto");
test("rechazo del valor vacío", guardarCodigo("") === false, "aceptó un valor vacío");

$filasDespues = contarFilas();
test("los inválidos no insertan filas", $filasAntes == $filasDespues, "se insertaron filas de más");

// ----------------------------------------------------------
// 3. almacenamiento correcto
// ----------------------------------------------------------
guardarCodigo(111);
test("almacenamiento correcto", contarFilas() >= 1, "el código no se guardó");

// ----------------------------------------------------------
// 4. consulta correcta (guardo 111 y luego 222)
// ----------------------------------------------------------
guardarCodigo(222);
$resultado = consultarUltimoCodigo();
test("consulta correcta", $resultado->valor == 222, "no devolvió el último código");

// ----------------------------------------------------------
// 5. registro de la fecha de recepción
// ----------------------------------------------------------
$resultado = consultarUltimoCodigo();
$momento = strtotime($resultado->fecha);
$cercana = ($momento !== false) && (abs(time() - $momento) < 120);
test("registro de la fecha de recepción", $cercana, "la fecha no es válida o no es cercana");

// ----------------------------------------------------------
// recuento final
// ----------------------------------------------------------
echo "\n$testsCorrectos/$testsTotal tests correctos\n";

if ($testsCorrectos != $testsTotal) {
    exit(1);
}
