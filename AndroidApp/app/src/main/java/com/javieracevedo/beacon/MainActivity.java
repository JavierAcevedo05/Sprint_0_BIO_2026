package com.javieracevedo.beacon;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

// ----------------------------------------------------------
// ----------------------------------------------------------
// Pantalla principal: escanea el beacon, muestra el valor
// detectado (minor) y lo envía automáticamente al servidor.
//
// Toda la lógica está en LogicaFake; aquí solo se conectan las
// piezas (escáner real + cliente real) y se pinta el estado.
// ----------------------------------------------------------

public class MainActivity extends AppCompatActivity implements LogicaFake.ObservadorUI {

    // ------------------------------------------------------
    // ------------------------------------------------------
    // URL del servidor REST (PC en la red local).
    // ------------------------------------------------------
    public static final String URL_SERVIDOR = "http://172.20.10.4:8080/src/communication/guardarCodigo.php";

    private static final int CODIGO_PETICION_PERMISOS = 11223344;

    // ------------------------------------------------------
    // ------------------------------------------------------
    private TextView tvEstado;
    private TextView tvValor;
    private TextView tvResultado;

    private LogicaFake laLogica;

    // ------------------------------------------------------
    // ------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvEstado = findViewById(R.id.tvEstado);
        tvValor = findViewById(R.id.tvValor);
        tvResultado = findViewById(R.id.tvResultado);

        EscannerBeacon elEscanner = new EscannerBeaconBTLE(this);
        ClienteREST elCliente = new ClienteRESTHttp(URL_SERVIDOR);

        laLogica = new LogicaFake(elEscanner, elCliente);
        laLogica.setObservadorUI(this);

        tvValor.setText(getString(R.string.sin_valor));
        tvResultado.setText("");
        pintarEstado(laLogica.obtenerEstado());

        pedirPermisos();
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    private void pedirPermisos() {

        ArrayList<String> necesarios = new ArrayList<String>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            necesarios.add(Manifest.permission.BLUETOOTH_SCAN);
            necesarios.add(Manifest.permission.BLUETOOTH_CONNECT);
            // Para escanear beacons hace falta ubicación precisa
            // (ya no usamos neverForLocation, que filtraba los iBeacons).
            necesarios.add(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            necesarios.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        ArrayList<String> faltan = new ArrayList<String>();
        for (String permiso : necesarios) {
            if (ContextCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                faltan.add(permiso);
            }
        }

        if (!faltan.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    faltan.toArray(new String[0]), CODIGO_PETICION_PERMISOS);
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    public void botonEscanearPulsado(View v) {

        pedirPermisos();

        boolean ok = laLogica.empezarEscaneo();
        if (!ok) {
            tvResultado.setText(getString(R.string.no_se_pudo_escanear));
        } else {
            tvValor.setText(getString(R.string.sin_valor));
            tvResultado.setText(getString(R.string.esperando_beacon));
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    public void botonDetenerPulsado(View v) {
        laLogica.detenerEscaneo();
    }

    // ------------------------------------------------------
    // Callbacks de la lógica (siempre en el hilo principal).
    // ------------------------------------------------------
    public void alCambiarEstado(EstadoLogica estado) {
        pintarEstado(estado);
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    public void alDetectarCodigo(final int valor) {
        tvValor.setText(getString(R.string.valor_detectado, valor));
        // enviamos el código automáticamente
        laLogica.enviarCodigo();
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    public void alTerminarEnvio(boolean ok, String error) {
        if (ok) {
            tvResultado.setText(getString(R.string.codigo_enviado));
        } else {
            tvResultado.setText(getString(R.string.error_envio, error));
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    private void pintarEstado(EstadoLogica estado) {
        tvEstado.setText(getString(R.string.estado, estado.toString()));
    }
}
