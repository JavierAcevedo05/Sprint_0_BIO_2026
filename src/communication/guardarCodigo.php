<?php

require_once __DIR__ . "/../business_logic/guardarCodigo.php";

// ----------------------------------------------------------
//
// POST /src/communication/guardarCodigo.php
// cuerpo JSON: {"valor":Entero}
//
// valor:Entero -> guardarCodigo() -> bool | error:Texto
//
// @return
//  resultado:bool
//  error:Texto   (solo cuando resultado = false)
//
// ----------------------------------------------------------

header("Content-Type: application/json; charset=utf-8");

$objetoResultado = new stdClass;

// 1. recoge los parámetros
$cuerpo = file_get_contents("php://input");
$datos = json_decode($cuerpo, true);

// el cuerpo debe ser un JSON con un campo "valor"
if ($datos === null || !is_array($datos) || !array_key_exists("valor", $datos)) {
    $objetoResultado->resultado = false;
    $objetoResultado->error = "cuerpo mal formado";
    echo json_encode($objetoResultado);
    return;
}

// 2. llama a la lógica de negocio
$ok = guardarCodigo($datos["valor"]);

// 3. devuelve el resultado en JSON
if ($ok) {
    $objetoResultado->resultado = true;
} else {
    $objetoResultado->resultado = false;
    $objetoResultado->error = "código no válido";
}

echo json_encode($objetoResultado);
