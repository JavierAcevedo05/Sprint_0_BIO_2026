// ----------------------------------------------------------
// ----------------------------------------------------------
// Simulación del escáner: no usa Bluetooth real.
// El test (o quien sea) llama a simularLlegadaBeacon() para
// hacer llegar un código como si lo hubiera detectado.
// ----------------------------------------------------------

public class EscannerBeaconFake implements EscannerBeacon {

    private Observador observador = null;
    private boolean escaneando = false;

    // ------------------------------------------------------
    //
    // observador:Observador -> registrarObservador() -> nada
    //
    // ------------------------------------------------------
    public void registrarObservador(Observador observador) {
        this.observador = observador;
    }

    // ------------------------------------------------------
    //
    // -> empezarEscaneo() -> bool
    //
    // ------------------------------------------------------
    public boolean empezarEscaneo() {
        if (this.escaneando) {
            return false; // ya estaba escaneando
        }
        this.escaneando = true;
        return true;
    }

    // ------------------------------------------------------
    //
    // -> detenerEscaneo() -> bool
    //
    // ------------------------------------------------------
    public boolean detenerEscaneo() {
        if (!this.escaneando) {
            return false; // no estaba escaneando
        }
        this.escaneando = false;
        return true;
    }

    // ------------------------------------------------------
    //
    // valor:Entero -> simularLlegadaBeacon() -> bool
    //
    // Simula que ha llegado un beacon con ese código.
    //
    // ------------------------------------------------------
    public boolean simularLlegadaBeacon(int valor) {
        if (!this.escaneando) {
            return false; // no se puede detectar si no se escanea
        }
        if (this.observador != null) {
            this.observador.alDetectarCodigo(valor);
        }
        return true;
    }
}
