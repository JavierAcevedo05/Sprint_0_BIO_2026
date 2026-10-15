package com.javieracevedo.beacon;

// ----------------------------------------------------------
// ----------------------------------------------------------
// Interfaz del envío del código al servidor.
//
// La lógica no sabe si la petición es HTTP real o un doble:
// solo usa esta interfaz. En los tests se sustituye por un doble.
// ----------------------------------------------------------

public interface ClienteREST {

    // ------------------------------------------------------
    //
    // valor:Entero, respuesta:Respuesta -> enviarCodigo() -> nada
    //
    // Envía el código por POST; avisa por callback cuando
    // tenga la respuesta (código HTTP + cuerpo).
    //
    // ------------------------------------------------------
    void enviarCodigo(int valor, Respuesta respuesta);

    // ------------------------------------------------------
    // ------------------------------------------------------
    // Quien envíe el código recibe aquí la respuesta.
    // ------------------------------------------------------
    interface Respuesta {
        void alResponder(int codigoHttp, String cuerpo);
    }
}
