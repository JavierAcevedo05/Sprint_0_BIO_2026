<?php

require_once __DIR__ . "/config.php";

// ----------------------------------------------------------
//
// -> consultarUltimoCodigo() -> (valor:Entero, fecha:Fecha) | error:Texto
//
// Devuelve el último código guardado (la fila de mayor id).
// Si la BD está vacía, devuelve un error con texto.
//
// ----------------------------------------------------------

function consultarUltimoCodigo() {

    $objetoResultado = new stdClass;

    $conexion = getConexion();

    $resultado = $conexion->query(
        "SELECT valor, fecha FROM CODIGO ORDER BY id DESC LIMIT 1"
    );

    // ¿hay fila?
    if ($resultado === false || $resultado->num_rows == 0) {
        $objetoResultado->error = "no hay códigos guardados";
        $conexion->close();
        return $objetoResultado;
    }

    $fila = $resultado->fetch_assoc();

    $objetoResultado->valor = (int) $fila["valor"];
    $objetoResultado->fecha = $fila["fecha"];
    $objetoResultado->error = 0;

    $conexion->close();

    return $objetoResultado;
}
