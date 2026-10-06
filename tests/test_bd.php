<?php

// ----------------------------------------------------------
//
// Tests de la base de datos (bd/crear_bd.sql)
// Ejecutar con: php tests/test_bd.php
//
// ----------------------------------------------------------

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
// -> conexionServidor() -> mysqli
//
// Conexión al servidor MySQL sin seleccionar base de datos.
//
// ----------------------------------------------------------

function conexionServidor() {
    return new mysqli("127.0.0.1", "root", "");
}

// ----------------------------------------------------------
//
// -> prepararBaseDeDatos() -> mysqli
//
// Ejecuta crear_bd.sql y devuelve la conexión a bd_codigos.
//
// ----------------------------------------------------------

function prepararBaseDeDatos() {

    $servidor = conexionServidor();
    $sql = file_get_contents(__DIR__ . "/../bd/crear_bd.sql");

    $servidor->multi_query($sql);
    while ($servidor->more_results()) {
        $servidor->next_result();
    }
    $servidor->close();

    $conexion = new mysqli("127.0.0.1", "root", "", "bd_codigos");
    $conexion->set_charset("utf8mb4");

    return $conexion;
}

$conexion = prepararBaseDeDatos();

// limpiamos la tabla para poder repetir los tests
$conexion->query("TRUNCATE TABLE CODIGO");

// ----------------------------------------------------------
// 1. creación de la tabla
// ----------------------------------------------------------
$resultado = $conexion->query("SHOW TABLES LIKE 'CODIGO'");
test("creación de la tabla", $resultado->num_rows == 1, "la tabla CODIGO no existe");

// ----------------------------------------------------------
// 2. inserción de un código
// ----------------------------------------------------------
$conexion->query("INSERT INTO CODIGO (valor, fecha) VALUES (1234, NOW())");
$resultado = $conexion->query("SELECT COUNT(*) AS n FROM CODIGO");
$fila = $resultado->fetch_assoc();
test("inserción de un código", $fila["n"] == 1, "no se insertó la fila");

// ----------------------------------------------------------
// 3. consulta del último guardado
// ----------------------------------------------------------
$conexion->query("INSERT INTO CODIGO (valor, fecha) VALUES (222, NOW())");
$resultado = $conexion->query("SELECT valor FROM CODIGO ORDER BY id DESC LIMIT 1");
$fila = $resultado->fetch_assoc();
test("consulta del último guardado", $fila["valor"] == 222, "no devolvió el último código");

// ----------------------------------------------------------
// 4. restricción: valores fuera de rango
// ----------------------------------------------------------
$fallo = false;
try {
    $conexion->query("INSERT INTO CODIGO (valor, fecha) VALUES (70000, NOW())");
} catch (mysqli_sql_exception $e) {
    $fallo = true;
}
test("restricción de valor por encima del rango", $fallo, "aceptó un valor mayor de 65535");

$fallo = false;
try {
    $conexion->query("INSERT INTO CODIGO (valor, fecha) VALUES (-1, NOW())");
} catch (mysqli_sql_exception $e) {
    $fallo = true;
}
test("restricción de valor por debajo del rango", $fallo, "aceptó un valor menor de 0");

// ----------------------------------------------------------
// 5. restricción: campos vacíos (valor y fecha NULL)
// ----------------------------------------------------------
$fallo = false;
try {
    $conexion->query("INSERT INTO CODIGO (valor, fecha) VALUES (NULL, NOW())");
} catch (mysqli_sql_exception $e) {
    $fallo = true;
}
test("restricción de valor vacío", $fallo, "aceptó un valor NULL");

$fallo = false;
try {
    $conexion->query("INSERT INTO CODIGO (valor, fecha) VALUES (1234, NULL)");
} catch (mysqli_sql_exception $e) {
    $fallo = true;
}
test("restricción de fecha vacía", $fallo, "aceptó una fecha NULL");

// dejamos la tabla limpia al terminar
$conexion->query("TRUNCATE TABLE CODIGO");

$conexion->close();

// ----------------------------------------------------------
// recuento final
// ----------------------------------------------------------
echo "\n$testsCorrectos/$testsTotal tests correctos\n";

if ($testsCorrectos != $testsTotal) {
    exit(1);
}
