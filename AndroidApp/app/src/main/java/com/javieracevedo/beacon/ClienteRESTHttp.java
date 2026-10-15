package com.javieracevedo.beacon;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// ----------------------------------------------------------
// ----------------------------------------------------------
// Implementación real de ClienteREST para Android.
//
// Hace el POST al servidor en un hilo de fondo (no se puede
// usar la red en el hilo principal) y avisa por callback en el
// hilo principal, para poder tocar la UI con seguridad.
// ----------------------------------------------------------

public class ClienteRESTHttp implements ClienteREST {

    private static final String ETIQUETA_LOG = ">>>>";

    private final String url;
    private final Handler handlerPrincipal = new Handler(Looper.getMainLooper());

    // ------------------------------------------------------
    //
    // url:Texto -> ClienteRESTHttp() -> ClienteRESTHttp
    //
    // ------------------------------------------------------
    public ClienteRESTHttp(String url) {
        this.url = url;
    }

    // ------------------------------------------------------
    //
    // valor:Entero, respuesta:Respuesta -> enviarCodigo() -> nada
    //
    // Envía "{\"valor\": N}" por POST y avisa por callback.
    //
    // ------------------------------------------------------
    public void enviarCodigo(final int valor, final Respuesta respuesta) {

        Thread hilo = new Thread(new Runnable() {
            public void run() {

                int codigoHttp = -1;
                String cuerpo = "";

                try {
                    URL destino = java.net.URI.create(ClienteRESTHttp.this.url).toURL();
                    HttpURLConnection conexion = (HttpURLConnection) destino.openConnection();

                    conexion.setRequestMethod("POST");
                    conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    conexion.setDoInput(true);
                    conexion.setDoOutput(true);

                    String json = "{\"valor\": " + valor + "}";
                    DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
                    salida.writeBytes(json);
                    salida.flush();
                    salida.close();

                    codigoHttp = conexion.getResponseCode();

                    InputStream is = (codigoHttp < 400)
                            ? conexion.getInputStream() : conexion.getErrorStream();
                    if (is != null) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuilder acumulador = new StringBuilder();
                        String linea;
                        while ((linea = br.readLine()) != null) {
                            acumulador.append(linea);
                        }
                        cuerpo = acumulador.toString();
                    }

                    conexion.disconnect();

                } catch (Exception ex) {
                    codigoHttp = -1;
                    cuerpo = "fallo de red: " + ex.getMessage();
                    Log.d(ETIQUETA_LOG, " enviarCodigo(): " + ex.getMessage());
                }

                final int codigoFinal = codigoHttp;
                final String cuerpoFinal = cuerpo;

                ClienteRESTHttp.this.handlerPrincipal.post(new Runnable() {
                    public void run() {
                        respuesta.alResponder(codigoFinal, cuerpoFinal);
                    }
                });
            }
        });

        hilo.start();
    }
}
