package com.javieracevedo.beacon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

// ----------------------------------------------------------
// ----------------------------------------------------------
// Escáner BLE real: implementa la interfaz EscannerBeacon.
//
// Filtra los anuncios iBeacon del beacon del proyecto
// (companyID de Apple + tipo iBeacon), comprueba que el UUID
// coincide con el nuestro y notifica el valor (minor) al
// observador. El callback se entrega siempre en el hilo
// principal para poder tocar la UI con seguridad.
// ----------------------------------------------------------

public class EscannerBeaconBTLE implements EscannerBeacon {

    // ------------------------------------------------------
    // ------------------------------------------------------
    private static final String ETIQUETA_LOG = ">>>>";

    private static final String UUID_BEACON = "EPSG-GTI-PROY-3A";
    private static final int COMPANY_ID_APPLE = 0x004C;

    private final Context contexto;
    private Observador observador = null;

    private BluetoothLeScanner elEscanner = null;
    private ScanCallback callbackDelEscaneo = null;
    private boolean escaneando = false;

    private final Handler handlerPrincipal = new Handler(Looper.getMainLooper());

    // ------------------------------------------------------
    // ------------------------------------------------------
    public EscannerBeaconBTLE(Context contexto) {
        this.contexto = contexto.getApplicationContext();
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    public void registrarObservador(Observador observador) {
        this.observador = observador;
    }

    // ------------------------------------------------------
    // Permisos necesarios para escanear según la versión.
    // ------------------------------------------------------
    private boolean tengoPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_SCAN)
                        == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
        }
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    public boolean empezarEscaneo() {
        Log.d(ETIQUETA_LOG, " empezarEscaneo(): empieza");

        if (this.escaneando) {
            return false; // ya estaba escaneando
        }

        if (!tengoPermisos()) {
            Log.d(ETIQUETA_LOG, " empezarEscaneo(): faltan permisos");
            return false;
        }

        BluetoothManager gestor = (BluetoothManager) contexto.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bta = (gestor != null) ? gestor.getAdapter() : null;

        if (bta == null || !bta.isEnabled()) {
            Log.d(ETIQUETA_LOG, " empezarEscaneo(): no hay adaptador Bluetooth encendido");
            return false;
        }

        this.elEscanner = bta.getBluetoothLeScanner();
        if (this.elEscanner == null) {
            Log.d(ETIQUETA_LOG, " empezarEscaneo(): no se obtuvo escáner BTLE");
            return false;
        }

        this.callbackDelEscaneo = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult resultado) {
                super.onScanResult(callbackType, resultado);
                procesarResultado(resultado);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(ETIQUETA_LOG, " onScanFailed(): error " + errorCode);
            }
        };

        ScanSettings ajustes = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        // Escaneamos SIN filtro a nivel de sistema y decidimos en código:
        // los filtros de manufacturer data fallan en algunas ROM (OPPO/ColorOS)
        // y un filtro por dirección MAC bloquearía el escaneo si cambia la MAC.
        this.elEscanner.startScan(null, ajustes, this.callbackDelEscaneo);
        this.escaneando = true;

        Log.d(ETIQUETA_LOG, " empezarEscaneo(): termina");
        return true;
    }

    // ------------------------------------------------------
    // ------------------------------------------------------
    public boolean detenerEscaneo() {
        Log.d(ETIQUETA_LOG, " detenerEscaneo(): empieza");

        if (!this.escaneando) {
            return false;
        }

        if (this.elEscanner != null && this.callbackDelEscaneo != null && tengoPermisos()) {
            try {
                this.elEscanner.stopScan(this.callbackDelEscaneo);
            } catch (Exception ex) {
                Log.d(ETIQUETA_LOG, " detenerEscaneo(): " + ex.getMessage());
            }
        }

        this.callbackDelEscaneo = null;
        this.escaneando = false;

        Log.d(ETIQUETA_LOG, " detenerEscaneo(): termina");
        return true;
    }

    // ------------------------------------------------------
    // Parsea el anuncio y, si es nuestro beacon, notifica el
    // valor (minor) en el hilo principal.
    // ------------------------------------------------------
    private void procesarResultado(ScanResult resultado) {
        byte[] bytes = (resultado.getScanRecord() != null)
                ? resultado.getScanRecord().getBytes() : null;
        String nombre = (resultado.getScanRecord() != null)
                ? resultado.getScanRecord().getDeviceName() : null;

        boolean pareceBeacon = (bytes != null) && pareceIBeacon(bytes);
        boolean tieneAppleId = (bytes != null) && contienePar(bytes, (byte) 0x4C, (byte) 0x00);
        boolean nombreInteresa = (nombre != null)
                && (nombre.toUpperCase().contains("GTI") || nombre.toUpperCase().contains("EPSG"));

        // registramos solo lo relevante: iBeacon, manufacturer de Apple (4c 00)
        // o nombres de nuestro beacon; el resto se ignora.
        if (!pareceBeacon && !tieneAppleId && !nombreInteresa) {
            return;
        }

        Log.d(ETIQUETA_LOG, " resultado: " + resultado.getDevice().getAddress()
                + " name=" + nombre
                + " rssi=" + resultado.getRssi()
                + " pareceIBeacon=" + pareceBeacon
                + " appleId=" + tieneAppleId
                + " bytes=" + Utilidades.bytesToHexString(bytes));

        if (!pareceBeacon) {
            return;
        }

        try {
            TramaIBeacon tib = new TramaIBeacon(bytes);

            String uuid = Utilidades.bytesToString(tib.getUUID());
            if (!UUID_BEACON.equals(uuid)) {
                Log.d(ETIQUETA_LOG, " iBeacon con otro UUID: " + uuid);
                return; // no es nuestro beacon
            }

            final int valor = Utilidades.bytesToInt(tib.getMinor());

            Log.d(ETIQUETA_LOG, " procesarResultado(): beacon detectado, valor = " + valor);

            if (this.observador != null) {
                this.handlerPrincipal.post(new Runnable() {
                    public void run() {
                        observador.alDetectarCodigo(valor);
                    }
                });
            }
        } catch (Exception ex) {
            Log.d(ETIQUETA_LOG, " procesarResultado(): no se pudo parsear: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------
    // Busca la secuencia de Manufacturer Specific Data de un
    // iBeacon de Apple: ff 4c 00 02 15 (en cualquier posición).
    // ------------------------------------------------------
    private boolean pareceIBeacon(byte[] bytes) {
        for (int i = 0; (i + 4) < bytes.length; i++) {
            if ((bytes[i] & 0xFF) == 0xFF
                    && (bytes[i + 1] & 0xFF) == 0x4C
                    && (bytes[i + 2] & 0xFF) == 0x00
                    && (bytes[i + 3] & 0xFF) == 0x02
                    && (bytes[i + 4] & 0xFF) == 0x15) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------
    // ¿Aparecen dos bytes consecutivos (p. ej. 0x4C 0x00)?
    // ------------------------------------------------------
    private boolean contienePar(byte[] bytes, byte b1, byte b2) {
        for (int i = 0; (i + 1) < bytes.length; i++) {
            if (bytes[i] == b1 && bytes[i + 1] == b2) {
                return true;
            }
        }
        return false;
    }
}
