<?php

require_once __DIR__ . "/../business_logic/consultarUltimoCodigo.php";

// ----------------------------------------------------------
//
// GET /src/communication/consultarCodigo.php
// (sin parámetros)
//
// -> consultarUltimoCodigo() -> (valor:Entero, fecha:Fecha) | error:Texto
//
// @return
//  valor:Entero
//  fecha:Fecha
//  error:Entero   (0 cuando ha ido bien)
//  error:Texto    (cuando no hay códigos guardados)
//
// ----------------------------------------------------------

header("Content-Type: application/json; charset=utf-8");

// recoge los parámetros (ninguno), llama a la lógica y devuelve JSON
$objetoResultado = consultarUltimoCodigo();

echo json_encode($objetoResultado);
