<?php

// ----------------------------------------------------------
//
// -> getConexion() -> mysqli
//
// Devuelve la conexión a la base de datos bd_codigos.
// Es el único sitio con los datos de conexión.
//
// ----------------------------------------------------------

function getConexion() {

    $host      = "127.0.0.1";
    $usuario   = "root";
    $password  = "";
    $baseDatos = "bd_codigos";

    // abrimos la conexión con MySQL
    $conexion = new mysqli($host, $usuario, $password, $baseDatos);

    // si falla, lo tratamos como error de ejecución
    if ($conexion->connect_errno) {
        throw new Exception("Error de conexión a la BD: " . $conexion->connect_error);
    }

    // forzamos utf8mb4 para que los textos con acentos se guarden bien
    $conexion->set_charset("utf8mb4");

    return $conexion;
}
