<?php

require_once __DIR__ . "/config.php";

// ----------------------------------------------------------
//
// valor:Entero -> guardarCodigo() -> bool
//
// Regla de negocio: un código válido es un entero entre 0 y 65535.
// Si es válido lo inserta con la fecha del servidor; si no, devuelve false.
//
// ----------------------------------------------------------

function guardarCodigo($valor) {

    // ¿es un entero entre 0 y 65535?
    $valorValido = filter_var(
        $valor,
        FILTER_VALIDATE_INT,
        array("options" => array("min_range" => 0, "max_range" => 65535))
    );

    if ($valorValido === false) {
        // no es un entero válido -> se rechaza
        return false;
    }

    // lo inserta en la tabla con fecha = ahora.
    // se usa una sentencia preparada para evitar inyección SQL;
    // la "i" de bind_param indica que el valor es un entero.
    $conexion = getConexion();

    $sentencia = $conexion->prepare(
        "INSERT INTO CODIGO (valor, fecha) VALUES (?, NOW())"
    );
    $sentencia->bind_param("i", $valorValido);

    $ok = $sentencia->execute();

    $sentencia->close();
    $conexion->close();

    return $ok;
}
