// ----------------------------------------------------------
// ----------------------------------------------------------
// Lógica fake del teléfono: máquina de estados que simula la
// detección del beacon y el envío del código al servidor.
//
// No usa Bluetooth ni red reales: depende solo de las interfaces
// EscannerBeacon y ClienteREST, que se inyectan.
// ----------------------------------------------------------

public class LogicaFake implements EscannerBeacon.Observador {

    private EstadoLogica estado = EstadoLogica.INACTIVO;

    private Integer valorDetectado = null;

    private EscannerBeacon escanner;
    private ClienteREST cliente;

    private ObservadorUI observadorUI = null;

    // guarda el resultado del último envío (lo rellena el callback)
    private Resultado resultadoEnvio = null;

    // ------------------------------------------------------
    //
    // escanner:EscannerBeacon, cliente:ClienteREST -> LogicaFake() -> LogicaFake
    //
    // ------------------------------------------------------
    public LogicaFake(EscannerBeacon escanner, ClienteREST cliente) {
        this.escanner = escanner;
        this.cliente = cliente;
        this.escanner.registrarObservador(this);
    }

    // ------------------------------------------------------
    //
    // observador:ObservadorUI -> setObservadorUI() -> nada
    //
    // ------------------------------------------------------
    public void setObservadorUI(ObservadorUI observadorUI) {
        this.observadorUI = observadorUI;
    }

    // ------------------------------------------------------
    //
    // -> obtenerEstado() -> EstadoLogica
    //
    // ------------------------------------------------------
    public EstadoLogica obtenerEstado() {
        return this.estado;
    }

    // ------------------------------------------------------
    //
    // -> empezarEscaneo() -> bool
    //
    // ------------------------------------------------------
    public boolean empezarEscaneo() {

        if (this.estado != EstadoLogica.INACTIVO) {
            return false; // ya se estaba escaneando
        }

        this.valorDetectado = null;

        boolean ok = this.escanner.empezarEscaneo();

        if (!ok) {
            return false;
        }

        this.estado = EstadoLogica.ESCANEANDO;
        notificarEstado();

        return true;
    }

    // ------------------------------------------------------
    //
    // -> detenerEscaneo() -> bool
    //
    // ------------------------------------------------------
    public boolean detenerEscaneo() {

        if (this.estado == EstadoLogica.INACTIVO) {
            return false; // ya estaba inactivo
        }

        this.escanner.detenerEscaneo();

        this.estado = EstadoLogica.INACTIVO;
        this.valorDetectado = null;

        notificarEstado();

        return true;
    }

    // ------------------------------------------------------
    //
    // -> obtenerCodigoDetectado() -> Entero | error:Texto
    //
    // ------------------------------------------------------
    public Resultado obtenerCodigoDetectado() {

        if (this.valorDetectado == null) {
            return Resultado.error("no hay código detectado");
        }

        return Resultado.valor(this.valorDetectado);
    }

    // ------------------------------------------------------
    //
    // -> enviarCodigo() -> bool | error:Texto
    //
    // Envía el código detectado al servidor a través del ClienteREST.
    //
    // ------------------------------------------------------
    public Resultado enviarCodigo() {

        if (this.estado != EstadoLogica.DETECTADO || this.valorDetectado == null) {
            return Resultado.error("no hay código detectado");
        }

        final int valorAEnviar = this.valorDetectado;

        this.resultadoEnvio = null;

        // la lógica delega el envío en el cliente (real o doble)
        this.cliente.enviarCodigo(valorAEnviar, new ClienteREST.Respuesta() {
            public void alResponder(int codigoHttp, String cuerpo) {
                procesarRespuestaEnvio(valorAEnviar, codigoHttp, cuerpo);
            }
        });

        // el cliente responde de forma síncrona (doble o HTTP)
        if (this.resultadoEnvio == null) {
            return Resultado.error("sin respuesta del servidor");
        }

        return this.resultadoEnvio;
    }

    // ------------------------------------------------------
    //
    // valor:Entero -> alDetectarCodigo() -> nada
    //
    // callback del escáner: llega un beacon con un código.
    //
    // ------------------------------------------------------
    public void alDetectarCodigo(int valor) {

        if (this.estado != EstadoLogica.ESCANEANDO) {
            return; // ignoramos si no estamos escaneando
        }

        this.valorDetectado = valor;
        this.estado = EstadoLogica.DETECTADO;

        notificarEstado();
        notificarCodigoDetectado(valor);
    }

    // ------------------------------------------------------
    //
    // valor:Entero, codigoHttp:Entero, cuerpo:Texto -> procesarRespuestaEnvio() -> nada
    //
    // Traduce la respuesta del servidor a un resultado.
    //
    // ------------------------------------------------------
    private void procesarRespuestaEnvio(int valor, int codigoHttp, String cuerpo) {

        String cuerpoSinEspacios = (cuerpo == null) ? "" : cuerpo.replace(" ", "");

        boolean ok = codigoHttp >= 200 && codigoHttp < 400
                && cuerpoSinEspacios.contains("\"resultado\":true");

        if (ok) {
            this.estado = EstadoLogica.ENVIADO;
            notificarEstado();
            this.resultadoEnvio = Resultado.valor(valor);
        } else {
            String error;
            if (codigoHttp == -1) {
                error = "fallo de red";
            } else {
                error = extraerError(cuerpo);
            }
            this.resultadoEnvio = Resultado.error(error);
        }

        notificarFinEnvio(ok, ok ? null : this.resultadoEnvio.error);
    }

    // ------------------------------------------------------
    //
    // cuerpo:Texto -> extraerError() -> Texto
    //
    // Saca el campo "error" del JSON del servidor.
    //
    // ------------------------------------------------------
    private static String extraerError(String cuerpo) {

        if (cuerpo == null) {
            return "el servidor rechazó el código";
        }

        int marca = cuerpo.indexOf("\"error\"");
        if (marca < 0) {
            return "el servidor rechazó el código";
        }

        int inicio = cuerpo.indexOf('"', marca + 7);
        if (inicio < 0) {
            return "el servidor rechazó el código";
        }

        int fin = cuerpo.indexOf('"', inicio + 1);
        if (fin < 0) {
            return "el servidor rechazó el código";
        }

        return cuerpo.substring(inicio + 1, fin);
    }

    // ------------------------------------------------------
    //
    // -> notificarEstado() -> nada
    //
    // ------------------------------------------------------
    private void notificarEstado() {
        if (this.observadorUI != null) {
            this.observadorUI.alCambiarEstado(this.estado);
        }
    }

    // ------------------------------------------------------
    //
    // valor:Entero -> notificarCodigoDetectado() -> nada
    //
    // ------------------------------------------------------
    private void notificarCodigoDetectado(int valor) {
        if (this.observadorUI != null) {
            this.observadorUI.alDetectarCodigo(valor);
        }
    }

    // ------------------------------------------------------
    //
    // ok:VoF, error:Texto -> notificarFinEnvio() -> nada
    //
    // ------------------------------------------------------
    private void notificarFinEnvio(boolean ok, String error) {
        if (this.observadorUI != null) {
            this.observadorUI.alTerminarEnvio(ok, error);
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    // Callbacks hacia la UI.
    // ------------------------------------------------------
    public interface ObservadorUI {
        void alCambiarEstado(EstadoLogica estado);
        void alDetectarCodigo(int valor);
        void alTerminarEnvio(boolean ok, String error);
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    // Resultado de una operación: un valor o un error de texto.
    // ------------------------------------------------------
    public static class Resultado {

        public final boolean hayError;
        public final int valor;
        public final String error;

        private Resultado(boolean hayError, int valor, String error) {
            this.hayError = hayError;
            this.valor = valor;
            this.error = error;
        }

        // --------------------------------------------------
        //
        // valor:Entero -> valor() -> Resultado
        //
        // --------------------------------------------------
        public static Resultado valor(int valor) {
            return new Resultado(false, valor, null);
        }

        // --------------------------------------------------
        //
        // error:Texto -> error() -> Resultado
        //
        // --------------------------------------------------
        public static Resultado error(String error) {
            return new Resultado(true, 0, error);
        }
    }
}
