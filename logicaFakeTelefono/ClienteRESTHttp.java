import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// ----------------------------------------------------------
// ----------------------------------------------------------
// Implementación real de ClienteREST: hace la petición HTTP
// al servidor (basada en el PeticionarioREST del curso).
// ----------------------------------------------------------

public class ClienteRESTHttp implements ClienteREST {

    private String url;

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
    // Hace un POST con el cuerpo {"valor": valor} y avisa por
    // callback con el código HTTP y el cuerpo de la respuesta.
    //
    // ------------------------------------------------------
    public void enviarCodigo(int valor, Respuesta respuesta) {

        int codigoHttp = -1;
        String cuerpo = "";

        try {
            URL destino = java.net.URI.create(this.url).toURL();
            HttpURLConnection conexion = (HttpURLConnection) destino.openConnection();

            conexion.setRequestMethod("POST");
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conexion.setDoInput(true);
            conexion.setDoOutput(true);

            // escribo el cuerpo
            String json = "{\"valor\": " + valor + "}";
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            salida.writeBytes(json);
            salida.flush();
            salida.close();

            // leo la respuesta
            codigoHttp = conexion.getResponseCode();

            InputStream is = (codigoHttp < 400) ? conexion.getInputStream() : conexion.getErrorStream();
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
            // fallo de red o de conexión
            codigoHttp = -1;
            cuerpo = "fallo de red: " + ex.getMessage();
        }

        // aviso por callback
        respuesta.alResponder(codigoHttp, cuerpo);
    }
}
