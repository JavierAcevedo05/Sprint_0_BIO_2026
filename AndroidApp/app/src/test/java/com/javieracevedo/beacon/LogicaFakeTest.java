package com.javieracevedo.beacon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// ----------------------------------------------------------
//
// Tests JUnit de la lógica fake del teléfono.
//
// Es el mismo conjunto de casos de tests/TestLogicaFakeTelefono.java
// (Fase 4), pero portado a JUnit para correr con Gradle:
//
//   gradlew.bat test
//
// Se ejecutan sin Android ni Bluetooth: usan un doble del
// ClienteREST (éxito, rechazo, fallo de red) y un doble de la UI.
//
// ----------------------------------------------------------

public class LogicaFakeTest {

    // ------------------------------------------------------
    // ------------------------------------------------------
    // Doble del ClienteREST: simula el servidor.
    // ------------------------------------------------------
    static class ClienteRESTDoble implements ClienteREST {

        static final int EXITO = 0;
        static final int RECHAZO = 1;
        static final int FALLO_RED = 2;

        private final int modo;

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

    // --------------------------------------------------
    // 1. detección simulada de beacon
    // --------------------------------------------------
    @Test
    public void deteccionSimuladaDeBeacon() {

        EscannerBeaconFake escanner = new EscannerBeaconFake();
        LogicaFake logica = new LogicaFake(escanner, new ClienteRESTDoble(ClienteRESTDoble.EXITO));
        ObservadorDoble ui = new ObservadorDoble();
        logica.setObservadorUI(ui);

        assertTrue("no arrancó", logica.empezarEscaneo());
        assertEquals("estado incorrecto", EstadoLogica.ESCANEANDO, logica.obtenerEstado());

        escanner.simularLlegadaBeacon(1234);

        assertEquals("estado incorrecto", EstadoLogica.DETECTADO, logica.obtenerEstado());
        assertEquals("código no notificado", 1234, ui.ultimoCodigoDetectado);
        assertEquals("estado no notificado", EstadoLogica.DETECTADO, ui.ultimoEstado);
    }

    // --------------------------------------------------
    // 2. obtención del código
    // --------------------------------------------------
    @Test
    public void obtencionDelCodigo() {

        EscannerBeaconFake escanner = new EscannerBeaconFake();
        LogicaFake logica = new LogicaFake(escanner, new ClienteRESTDoble(ClienteRESTDoble.EXITO));

        LogicaFake.Resultado antes = logica.obtenerCodigoDetectado();
        assertTrue("no dio error", antes.hayError);

        logica.empezarEscaneo();
        escanner.simularLlegadaBeacon(4321);

        LogicaFake.Resultado despues = logica.obtenerCodigoDetectado();
        assertFalse("no devolvió el código", despues.hayError);
        assertEquals("no devolvió el código", 4321, despues.valor);
    }

    // --------------------------------------------------
    // 3. envío simulado (el servidor responde bien)
    // --------------------------------------------------
    @Test
    public void envioCorrecto() {

        EscannerBeaconFake escanner = new EscannerBeaconFake();
        ClienteRESTDoble cliente = new ClienteRESTDoble(ClienteRESTDoble.EXITO);
        LogicaFake logica = new LogicaFake(escanner, cliente);
        ObservadorDoble ui = new ObservadorDoble();
        logica.setObservadorUI(ui);

        logica.empezarEscaneo();
        escanner.simularLlegadaBeacon(1234);
        LogicaFake.Resultado envio = logica.enviarCodigo();

        assertFalse("devolvió error", envio.hayError);
        assertEquals("estado incorrecto", EstadoLogica.ENVIADO, logica.obtenerEstado());
        assertEquals("cuerpo distinto", "{\"valor\": 1234}", cliente.ultimoCuerpoEnviado);
        assertTrue("no notificó el fin", Boolean.TRUE.equals(ui.ultimoEnvioOk));
    }

    // --------------------------------------------------
    // 4a. enviar sin código detectado
    // --------------------------------------------------
    @Test
    public void errorEnviarSinCodigoDetectado() {

        EscannerBeaconFake escanner = new EscannerBeaconFake();
        LogicaFake logica = new LogicaFake(escanner, new ClienteRESTDoble(ClienteRESTDoble.EXITO));

        LogicaFake.Resultado sinCodigo = logica.enviarCodigo();
        assertTrue("no dio error", sinCodigo.hayError);
    }

    // --------------------------------------------------
    // 4b. el servidor rechaza el código
    // --------------------------------------------------
    @Test
    public void errorServidorRechazaElCodigo() {

        EscannerBeaconFake escanner = new EscannerBeaconFake();
        LogicaFake logica = new LogicaFake(escanner, new ClienteRESTDoble(ClienteRESTDoble.RECHAZO));

        logica.empezarEscaneo();
        escanner.simularLlegadaBeacon(999);
        LogicaFake.Resultado rechazo = logica.enviarCodigo();

        assertTrue("no dio error", rechazo.hayError);
        assertEquals("estado incorrecto", EstadoLogica.DETECTADO, logica.obtenerEstado());
    }

    // --------------------------------------------------
    // 4c. fallo de red
    // --------------------------------------------------
    @Test
    public void errorFalloDeRed() {

        EscannerBeaconFake escanner = new EscannerBeaconFake();
        LogicaFake logica = new LogicaFake(escanner, new ClienteRESTDoble(ClienteRESTDoble.FALLO_RED));

        logica.empezarEscaneo();
        escanner.simularLlegadaBeacon(55);
        LogicaFake.Resultado falloRed = logica.enviarCodigo();

        assertTrue("no detectó el fallo de red", falloRed.hayError);
        assertEquals("no detectó el fallo de red", "fallo de red", falloRed.error);
    }

    // --------------------------------------------------
    // 4d. detener estando ya inactivo
    // --------------------------------------------------
    @Test
    public void errorDetenerEstandoInactivo() {

        EscannerBeaconFake escanner = new EscannerBeaconFake();
        LogicaFake logica = new LogicaFake(escanner, new ClienteRESTDoble(ClienteRESTDoble.EXITO));

        assertFalse("devolvió true", logica.detenerEscaneo());
    }
}
