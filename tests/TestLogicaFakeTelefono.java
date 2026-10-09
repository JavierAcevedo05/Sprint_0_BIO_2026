// ----------------------------------------------------------
//
// Tests de la lógica fake del teléfono.
//
// Se ejecuta con:
//   javac -d out logicaFakeTelefono/*.java tests/TestLogicaFakeTelefono.java
//   java -cp out TestLogicaFakeTelefono
//
// Usa un doble del ClienteREST para simular el servidor:
// respuesta correcta, rechazo y fallo de red.
//
// ----------------------------------------------------------

public class TestLogicaFakeTelefono {

    static int testsCorrectos = 0;
    static int testsTotal = 0;

    // ------------------------------------------------------
    //
    // nombre:Texto, condicion:VoF, motivo:Texto -> test() -> nada
    //
    // Imprime OK o FALLO y lleva el recuento final.
    //
    // ------------------------------------------------------
    static void test(String nombre, boolean condicion, String motivo) {
        testsTotal++;
        if (condicion) {
            testsCorrectos++;
            System.out.println("OK: " + nombre);
        } else {
            System.out.println("FALLO: " + nombre + (motivo.equals("") ? "" : " - " + motivo));
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    // main(): lanza todos los tests.
    // ------------------------------------------------------
    public static void main(String[] args) {

        // --------------------------------------------------
        // 1. detección simulada de beacon
        // --------------------------------------------------
        EscannerBeaconFake escanner1 = new EscannerBeaconFake();
        LogicaFake logica1 = new LogicaFake(escanner1, new ClienteRESTDoble(ClienteRESTDoble.EXITO));
        ObservadorDoble ui1 = new ObservadorDoble();
        logica1.setObservadorUI(ui1);

        test("detección: empezar escaneo", logica1.empezarEscaneo() == true, "no arrancó");
        test("detección: estado ESCANEANDO", logica1.obtenerEstado() == EstadoLogica.ESCANEANDO, "estado incorrecto");

        escanner1.simularLlegadaBeacon(1234);

        test("detección: estado DETECTADO", logica1.obtenerEstado() == EstadoLogica.DETECTADO, "estado incorrecto");
        test("detección: se notifica el código", ui1.ultimoCodigoDetectado == 1234, "código no notificado");
        test("detección: callback de estado", ui1.ultimoEstado == EstadoLogica.DETECTADO, "estado no notificado");

        // --------------------------------------------------
        // 2. obtención del código
        // --------------------------------------------------
        EscannerBeaconFake escanner2 = new EscannerBeaconFake();
        LogicaFake logica2 = new LogicaFake(escanner2, new ClienteRESTDoble(ClienteRESTDoble.EXITO));

        LogicaFake.Resultado antes = logica2.obtenerCodigoDetectado();
        test("obtener código: antes de detectar da error", antes.hayError == true, "no dio error");

        logica2.empezarEscaneo();
        escanner2.simularLlegadaBeacon(4321);

        LogicaFake.Resultado despues = logica2.obtenerCodigoDetectado();
        test("obtener código: después devuelve el código",
                despues.hayError == false && despues.valor == 4321, "no devolvió el código");

        // --------------------------------------------------
        // 3. envío simulado (el servidor responde bien)
        // --------------------------------------------------
        EscannerBeaconFake escanner3 = new EscannerBeaconFake();
        ClienteRESTDoble cliente3 = new ClienteRESTDoble(ClienteRESTDoble.EXITO);
        LogicaFake logica3 = new LogicaFake(escanner3, cliente3);
        ObservadorDoble ui3 = new ObservadorDoble();
        logica3.setObservadorUI(ui3);

        logica3.empezarEscaneo();
        escanner3.simularLlegadaBeacon(1234);
        LogicaFake.Resultado envio = logica3.enviarCodigo();

        test("envío: resultado correcto", envio.hayError == false, "devolvió error");
        test("envío: estado ENVIADO", logica3.obtenerEstado() == EstadoLogica.ENVIADO, "estado incorrecto");
        test("envío: el cuerpo enviado es el correcto",
                "{\"valor\": 1234}".equals(cliente3.ultimoCuerpoEnviado), "cuerpo distinto");
        test("envío: callback de fin correcto", Boolean.TRUE.equals(ui3.ultimoEnvioOk), "no notificó el fin");

        // --------------------------------------------------
        // 4. simulación de errores
        // --------------------------------------------------

        // 4a. enviar sin código detectado
        EscannerBeaconFake escanner4 = new EscannerBeaconFake();
        LogicaFake logica4 = new LogicaFake(escanner4, new ClienteRESTDoble(ClienteRESTDoble.EXITO));
        LogicaFake.Resultado sinCodigo = logica4.enviarCodigo();
        test("error: enviar sin código detectado", sinCodigo.hayError == true, "no dio error");

        // 4b. el servidor rechaza el código
        EscannerBeaconFake escanner5 = new EscannerBeaconFake();
        LogicaFake logica5 = new LogicaFake(escanner5, new ClienteRESTDoble(ClienteRESTDoble.RECHAZO));
        logica5.empezarEscaneo();
        escanner5.simularLlegadaBeacon(999);
        LogicaFake.Resultado rechazo = logica5.enviarCodigo();
        test("error: el servidor rechaza el código", rechazo.hayError == true, "no dio error");
        test("error: no pasa a ENVIADO tras rechazo",
                logica5.obtenerEstado() == EstadoLogica.DETECTADO, "estado incorrecto");

        // 4c. fallo de red
        EscannerBeaconFake escanner6 = new EscannerBeaconFake();
        LogicaFake logica6 = new LogicaFake(escanner6, new ClienteRESTDoble(ClienteRESTDoble.FALLO_RED));
        logica6.empezarEscaneo();
        escanner6.simularLlegadaBeacon(55);
        LogicaFake.Resultado falloRed = logica6.enviarCodigo();
        test("error: fallo de red", falloRed.hayError == true && "fallo de red".equals(falloRed.error),
                "no detectó el fallo de red");

        // 4d. detener estando ya inactivo
        EscannerBeaconFake escanner7 = new EscannerBeaconFake();
        LogicaFake logica7 = new LogicaFake(escanner7, new ClienteRESTDoble(ClienteRESTDoble.EXITO));
        test("error: detener estando inactivo", logica7.detenerEscaneo() == false, "devolvió true");

        // --------------------------------------------------
        // recuento final
        // --------------------------------------------------
        System.out.println("\n" + testsCorrectos + "/" + testsTotal + " tests correctos");

        if (testsCorrectos != testsTotal) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    // Doble del ClienteREST: simula el servidor.
    // ------------------------------------------------------
    static class ClienteRESTDoble implements ClienteREST {

        static final int EXITO = 0;
        static final int RECHAZO = 1;
        static final int FALLO_RED = 2;

        private int modo;

        int ultimoValor = -1;
        String ultimoCuerpoEnviado = null;

        ClienteRESTDoble(int modo) {
            this.modo = modo;
        }

        public void enviarCodigo(int valor, Respuesta respuesta) {

            this.ultimoValor = valor;
            this.ultimoCuerpoEnviado = "{\"valor\": " + valor + "}";

            if (this.modo == EXITO) {
                respuesta.alResponder(200, "{\"resultado\":true}");
            } else if (this.modo == RECHAZO) {
                respuesta.alResponder(200, "{\"resultado\":false,\"error\":\"código no válido\"}");
            } else {
                respuesta.alResponder(-1, "fallo de red");
            }
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    // Doble de la UI: guarda lo último que le han notificado.
    // ------------------------------------------------------
    static class ObservadorDoble implements LogicaFake.ObservadorUI {

        EstadoLogica ultimoEstado = null;
        int ultimoCodigoDetectado = -1;
        Boolean ultimoEnvioOk = null;
        String ultimoErrorEnvio = null;

        public void alCambiarEstado(EstadoLogica estado) {
            this.ultimoEstado = estado;
        }

        public void alDetectarCodigo(int valor) {
            this.ultimoCodigoDetectado = valor;
        }

        public void alTerminarEnvio(boolean ok, String error) {
            this.ultimoEnvioOk = ok;
            this.ultimoErrorEnvio = error;
        }
    }
}
