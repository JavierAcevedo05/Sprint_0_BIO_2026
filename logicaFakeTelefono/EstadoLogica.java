// ----------------------------------------------------------
// ----------------------------------------------------------
// Estados por los que pasa la lógica fake del teléfono.
// ----------------------------------------------------------

public enum EstadoLogica {

    // no se está escaneando
    INACTIVO,

    // buscando beacons
    ESCANEANDO,

    // se ha detectado un beacon con su código
    DETECTADO,

    // el código detectado ya se ha enviado al servidor
    ENVIADO
}
