// ----------------------------------------------------------
// ----------------------------------------------------------
// Interfaz del escáner de beacons.
//
// La lógica no sabe si el beacon es real o simulado: solo usa
// esta interfaz. En producción la implementa un escáner real.
// ----------------------------------------------------------

public interface EscannerBeacon {

    // ------------------------------------------------------
    //
    // observador:Observador -> registrarObservador() -> nada
    //
    // ------------------------------------------------------
    void registrarObservador(Observador observador);

    // ------------------------------------------------------
    //
    // -> empezarEscaneo() -> bool
    //
    // ------------------------------------------------------
    boolean empezarEscaneo();

    // ------------------------------------------------------
    //
    // -> detenerEscaneo() -> bool
    //
    // ------------------------------------------------------
    boolean detenerEscaneo();

    // ------------------------------------------------------
    // ------------------------------------------------------
    // Quien quiera enterarse de los beacons detectados
    // implementa esta interfaz.
    // ------------------------------------------------------
    interface Observador {
        void alDetectarCodigo(int valor);
    }
}
